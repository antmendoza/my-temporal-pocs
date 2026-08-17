"""SandboxWorkflow — the child workflow that owns one sandbox.

Its workflow ID *is* the sandbox ID so the parent can address it directly. On the
`sandbox-init` update it provisions the compute instance (which boots the in-VM
worker); `sandbox-execute-activity` runs the work on the sandbox's own task queue
via that in-VM worker; the `sandbox-stop` signal tears it down.

Replace-and-restore model: this child does NOT self-heal. If the micro-VM is lost
mid-activity, the timeout is tagged SANDBOX_WORKER_LOST and propagates to the parent,
which creates a fresh sandbox for the same session.
"""

from __future__ import annotations

import asyncio
from dataclasses import dataclass
from datetime import timedelta
from enum import IntEnum
from typing import Optional

from temporalio import workflow
from temporalio.common import RetryPolicy
from temporalio.exceptions import (
    ActivityError,
    ApplicationError,
    TimeoutError as TemporalTimeoutError,
)

with workflow.unsafe.imports_passed_through():
    from . import activities
    from .compute import (
        SANDBOX_WORKER_LOST,
        ProviderDetails,
        ProviderStatus,
    )

SANDBOX_WORKFLOW_TYPE = "SandboxWorkflow"
DEFAULT_OP_TIMEOUT = timedelta(minutes=10)

SANDBOX_INIT_UPDATE = "sandbox-init"
SANDBOX_EXECUTE_ACTIVITY_UPDATE = "sandbox-execute-activity"
SANDBOX_STOP_SIGNAL = "sandbox-stop"


class Lifecycle(IntEnum):
    PENDING = 0
    RUNNING = 1
    FAILED = 2


@dataclass
class SandboxInitInput:
    provider: ProviderDetails
    # Durable, VM-independent session key supplied by the parent. In-VM activities
    # checkpoint under it, so a *new* sandbox created for the same session resumes.
    session_ref: str = ""


@dataclass
class ExecuteActivityInput:
    sleepTimeSeconds: int


@dataclass
class ExecuteActivityResult:
    stdout: str = ""
    stderr: str = ""
    exit_code: int = 0


@workflow.defn(name=SANDBOX_WORKFLOW_TYPE)
class SandboxWorkflow:
    def __init__(self) -> None:
        self._lifecycle = Lifecycle.PENDING
        self._provider: Optional[ProviderDetails] = None
        self._status: Optional[ProviderStatus] = None
        self._session_ref: str = ""
        self._cancel_requested = False

    @workflow.run
    async def run(self, parent_workflow_id: str) -> None:
        # Handlers are registered via decorators, so they are available immediately.
        # Block until init is processed.
        await workflow.wait_condition(lambda: self._lifecycle != Lifecycle.PENDING)
        if self._lifecycle == Lifecycle.FAILED:
            return
        workflow.logger.info(
            "sandbox initialised (parent=%s, provider=%s)",
            parent_workflow_id,
            self._provider.type if self._provider else None,
        )
        # Stay alive until the parent signals stop, then tear the sandbox down.
        try:
            await workflow.wait_condition(lambda: self._cancel_requested)
        except asyncio.CancelledError:
            pass
        await self._cleanup()

    @workflow.signal(name=SANDBOX_STOP_SIGNAL)
    def stop(self) -> None:
        self._cancel_requested = True

    @workflow.update(name=SANDBOX_INIT_UPDATE)
    async def init(self, inp: SandboxInitInput) -> None:
        try:
            out = await workflow.execute_activity(
                activities.start_sandbox,
                activities.StartSandboxInput(inp.provider, self._task_queue_name()),
                start_to_close_timeout=DEFAULT_OP_TIMEOUT,
            )
            self._status = out.status
        except Exception:
            self._lifecycle = Lifecycle.FAILED
            raise
        self._provider = inp.provider
        self._session_ref = inp.session_ref
        self._lifecycle = Lifecycle.RUNNING

    @init.validator
    def _validate_init(self, inp: SandboxInitInput) -> None:
        if self._lifecycle != Lifecycle.PENDING:
            raise ApplicationError("sandbox already initialized", type="AlreadyInitialized")

    @workflow.update(name=SANDBOX_EXECUTE_ACTIVITY_UPDATE)
    async def execute_activity(self, inp: ExecuteActivityInput) -> ExecuteActivityResult:
        # The activity runs on the in-VM worker's task queue exactly once. If the
        # micro-VM is evicted mid-run (worker dies -> heartbeat_timeout) or nothing is
        # polling (schedule_to_start_timeout), tag the timeout SANDBOX_WORKER_LOST and
        # let it propagate to the parent, which recreates the sandbox. Any other
        # failure propagates unchanged so the parent does NOT recreate on it.
        try:
            out = await workflow.execute_activity(
                activities.execute_activity_in_sandbox,
                activities.ActivityInSandboxInput(
                    sleepTimeSeconds=inp.sleepTimeSeconds,
                    session_ref=self._session_ref,
                ),
                task_queue=self._task_queue_name(),
                start_to_close_timeout=DEFAULT_OP_TIMEOUT,
                heartbeat_timeout=timedelta(seconds=10),
                schedule_to_start_timeout=timedelta(seconds=30),
                retry_policy=RetryPolicy(maximum_attempts=1),
            )
        except ActivityError as e:
            if isinstance(e.cause, TemporalTimeoutError):
                raise ApplicationError(
                    f"in-VM worker lost: {e.cause}",
                    type=SANDBOX_WORKER_LOST,
                    non_retryable=True,
                ) from e
            raise
        return ExecuteActivityResult(stdout=out.result)

    @execute_activity.validator
    def _validate_execute_activity(self, inp: ExecuteActivityInput) -> None:
        if self._lifecycle != Lifecycle.RUNNING:
            raise ApplicationError("sandbox not running", type="NotRunning")

    async def _cleanup(self) -> None:
        if self._provider is None or self._status is None:
            return
        try:
            await workflow.execute_activity(
                activities.stop_sandbox,
                activities.StopSandboxInput(self._provider, self._status),
                start_to_close_timeout=DEFAULT_OP_TIMEOUT,
            )
        except asyncio.CancelledError:
            pass

    def _task_queue_name(self) -> str:
        return f"sandbox-{workflow.info().workflow_id}"
