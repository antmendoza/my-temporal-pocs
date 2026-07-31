"""SandboxWorkflow — the long-lived child workflow that owns one sandbox
(port of `sdk/workflow/workflow.go`).

Its workflow ID *is* the sandbox ID so the parent can address it directly. It
registers update/query/signal handlers, provisions the compute instance on the
`sandbox-init` update, runs commands via the `execute-command` activity, and
auto-suspends after an idle period. Suspend falls back to snapshot+stop for
providers without native suspend (e.g. the local provider), and the next command
transparently resumes from that snapshot.
"""

from __future__ import annotations

import asyncio
from dataclasses import dataclass
from datetime import timedelta
from enum import IntEnum
from typing import Optional

from temporalio import workflow
from temporalio.exceptions import ActivityError, ApplicationError

with workflow.unsafe.imports_passed_through():
    from . import activities
    from .compute import (
        NO_IDLE_TIMEOUT,
        CommandResult,
        ProviderDetails,
        ProviderSnapshot,
        ProviderStatus,
        SandboxPostSnapshotState,
    )

SANDBOX_WORKFLOW_TYPE = "SandboxWorkflow"
DEFAULT_OP_TIMEOUT = timedelta(minutes=10)
IDLE_AUTO_SUSPEND = timedelta(minutes=5)

# Update / query / signal names (match the Go constants for cross-language parity).
SANDBOX_INIT_UPDATE = "sandbox-init"
SANDBOX_EXECUTE_COMMAND_UPDATE = "sandbox-execute-command"
SANDBOX_SUSPEND_UPDATE = "sandbox-suspend"
SANDBOX_RESUME_UPDATE = "sandbox-resume"
SANDBOX_STATE_QUERY = "sandbox-state"
SANDBOX_STOP_SIGNAL = "sandbox-stop"


class Lifecycle(IntEnum):
    PENDING = 0
    RUNNING = 1
    SUSPENDED = 2
    FAILED = 3
    DELETED = 4


@dataclass
class SandboxInitInput:
    provider: ProviderDetails
    # 0 → use IDLE_AUTO_SUSPEND default; NO_IDLE_TIMEOUT (-1) → never auto-suspend.
    idle_timeout_seconds: float
    snapshot: Optional[ProviderSnapshot] = None


@dataclass
class ExecuteCommandInput:
    command: str
    disable_auto_resume: bool = False


@dataclass
class SandboxState:
    lifecycle: int
    instance_id: Optional[str]
    idle_timeout_seconds: float


@workflow.defn(name=SANDBOX_WORKFLOW_TYPE)
class SandboxWorkflow:
    def __init__(self) -> None:
        self._lifecycle = Lifecycle.PENDING
        self._provider: Optional[ProviderDetails] = None
        self._status: Optional[ProviderStatus] = None
        self._idle_timeout_seconds: float = 0.0
        self._cancel_requested = False
        # Bumped at the start and end of every command; the idle loop watches it
        # to know when activity happened and re-arm its timer.
        self._command_seq = 0
        # Number of commands currently executing; the idle loop never suspends
        # while a command is in flight.
        self._active_commands = 0
        # Set when suspended via snapshot (fallback for providers without native
        # suspend). Non-None means resume must restart from the snapshot.
        self._suspend_snapshot: Optional[ProviderSnapshot] = None

    @workflow.run
    async def run(self, parent_workflow_id: str) -> None:
        # Update/query/signal handlers are registered via decorators, so they are
        # available as soon as the workflow starts. Block until init is processed.
        await workflow.wait_condition(lambda: self._lifecycle != Lifecycle.PENDING)
        if self._lifecycle == Lifecycle.FAILED:
            return

        workflow.logger.info(
            "sandbox initialised (parent=%s, provider=%s)",
            parent_workflow_id,
            self._provider.type if self._provider else None,
        )

        # Idle auto-suspend loop. Runs in the main workflow coroutine (rather than
        # a detached task) so it is deterministic and cannot race an update
        # handler. Exits on stop signal or cancellation.
        try:
            await self._idle_loop()
        except asyncio.CancelledError:
            # Discard so the workflow shows as Cancelled, not Failed.
            pass

        await self._cleanup()

    async def _idle_loop(self) -> None:
        while not self._cancel_requested:
            idle_enabled = self._idle_timeout_seconds != NO_IDLE_TIMEOUT
            if self._lifecycle == Lifecycle.RUNNING and idle_enabled:
                seq = self._command_seq
                try:
                    await workflow.wait_condition(
                        lambda: self._cancel_requested
                        or self._command_seq != seq
                        or self._lifecycle != Lifecycle.RUNNING,
                        timeout=timedelta(seconds=self._effective_idle_timeout()),
                    )
                except asyncio.TimeoutError:
                    # Idle elapsed with no new command and none in flight → suspend.
                    if (
                        self._lifecycle == Lifecycle.RUNNING
                        and not self._cancel_requested
                        and self._active_commands == 0
                        and self._command_seq == seq
                    ):
                        await self._suspend()
            else:
                # Suspended or idle disabled: wait until we become idle-eligible
                # again (e.g. after a resume) or are told to stop.
                await workflow.wait_condition(
                    lambda: self._cancel_requested
                    or (
                        self._lifecycle == Lifecycle.RUNNING
                        and self._idle_timeout_seconds != NO_IDLE_TIMEOUT
                    )
                )

    # ----------------------------------------------------------------- queries

    @workflow.query(name=SANDBOX_STATE_QUERY)
    def state(self) -> SandboxState:
        return SandboxState(
            lifecycle=int(self._lifecycle),
            instance_id=self._status.instance_id if self._status else None,
            idle_timeout_seconds=self._idle_timeout_seconds,
        )

    # ----------------------------------------------------------------- signals

    @workflow.signal(name=SANDBOX_STOP_SIGNAL)
    def stop(self) -> None:
        self._cancel_requested = True

    # ------------------------------------------------------------------- init

    @workflow.update(name=SANDBOX_INIT_UPDATE)
    async def init(self, inp: SandboxInitInput) -> None:
        try:
            if inp.snapshot is not None:
                out = await workflow.execute_activity(
                    activities.start_sandbox_from_snapshot,
                    activities.StartFromSnapshotInput(
                        inp.provider, self._task_queue_name(), inp.snapshot
                    ),
                    start_to_close_timeout=DEFAULT_OP_TIMEOUT,
                )
                self._status = out.status
            else:
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
        self._idle_timeout_seconds = inp.idle_timeout_seconds
        self._lifecycle = Lifecycle.RUNNING

    @init.validator
    def _validate_init(self, inp: SandboxInitInput) -> None:
        if self._lifecycle != Lifecycle.PENDING:
            raise ApplicationError("sandbox already initialized", type="AlreadyInitialized")
        # Provider-type validity is enforced by the init activity's compute.lookup
        # (the provider registry is populated in the activity worker process, not
        # reliably inside the workflow sandbox), which fails init with
        # ProviderConfigError for an unknown type.

    # --------------------------------------------------------- execute-command

    @workflow.update(name=SANDBOX_EXECUTE_COMMAND_UPDATE)
    async def execute_command(self, inp: ExecuteCommandInput) -> CommandResult:
        # Bump the sequence so the idle loop re-arms, and mark a command in flight
        # so it won't suspend underneath us.
        self._command_seq += 1
        self._active_commands += 1
        try:
            if self._lifecycle == Lifecycle.SUSPENDED:
                await self._resume()

            out = await workflow.execute_activity(
                activities.execute_command,
                activities.ExecuteCommandActivityInput(
                    self._provider, self._status, inp.command
                ),
                start_to_close_timeout=DEFAULT_OP_TIMEOUT,
            )
        finally:
            self._active_commands -= 1
            self._command_seq += 1  # re-arm the idle timer from a fresh baseline
        return out.result

    @execute_command.validator
    def _validate_execute_command(self, inp: ExecuteCommandInput) -> None:
        if self._lifecycle in (Lifecycle.PENDING, Lifecycle.FAILED, Lifecycle.DELETED):
            raise ApplicationError("sandbox not initialized", type="NotInitialized")
        if self._lifecycle == Lifecycle.SUSPENDED and inp.disable_auto_resume:
            raise ApplicationError("sandbox is suspended", type="Suspended")

    # ------------------------------------------------------------- suspend

    @workflow.update(name=SANDBOX_SUSPEND_UPDATE)
    async def suspend(self) -> None:
        await self._suspend()

    @suspend.validator
    def _validate_suspend(self) -> None:
        if self._lifecycle in (Lifecycle.PENDING, Lifecycle.FAILED, Lifecycle.DELETED):
            raise ApplicationError("sandbox not initialized", type="NotInitialized")
        if self._lifecycle == Lifecycle.SUSPENDED:
            raise ApplicationError("sandbox already suspended", type="AlreadySuspended")

    async def _suspend(self) -> None:
        if self._status is None:
            raise ApplicationError(
                "invalid sandbox state for suspend",
                type="InvalidSandboxState",
                non_retryable=True,
            )
        try:
            await workflow.execute_activity(
                activities.suspend_sandbox,
                activities.SuspendSandboxInput(self._provider, self._status),
                start_to_close_timeout=DEFAULT_OP_TIMEOUT,
            )
        except ActivityError as e:
            # Provider without native suspend → fall back to snapshot-based suspend.
            cause = e.cause
            if isinstance(cause, ApplicationError) and cause.type == "ErrUnsupported":
                await self._suspend_via_snapshot()
                return
            raise
        self._lifecycle = Lifecycle.SUSPENDED

    async def _suspend_via_snapshot(self) -> None:
        out = await workflow.execute_activity(
            activities.snapshot_sandbox,
            activities.SnapshotSandboxInput(self._provider, self._status),
            start_to_close_timeout=DEFAULT_OP_TIMEOUT,
        )
        if out.snapshot is None:
            raise ApplicationError(
                "provider returned a nil snapshot; cannot use snapshot-based suspend",
                type="InvalidProviderBehavior",
            )
        if out.sandbox_state == int(SandboxPostSnapshotState.RUNNING):
            await workflow.execute_activity(
                activities.stop_sandbox,
                activities.StopSandboxInput(self._provider, self._status),
                start_to_close_timeout=DEFAULT_OP_TIMEOUT,
            )
        self._suspend_snapshot = out.snapshot
        self._lifecycle = Lifecycle.SUSPENDED

    # -------------------------------------------------------------- resume

    @workflow.update(name=SANDBOX_RESUME_UPDATE)
    async def resume(self) -> None:
        await self._resume()

    @resume.validator
    def _validate_resume(self) -> None:
        if self._lifecycle in (Lifecycle.PENDING, Lifecycle.FAILED, Lifecycle.DELETED):
            raise ApplicationError("sandbox not initialized", type="NotInitialized")
        if self._lifecycle != Lifecycle.SUSPENDED:
            raise ApplicationError("sandbox not suspended", type="NotSuspended")

    async def _resume(self) -> None:
        if self._suspend_snapshot is not None:
            out = await workflow.execute_activity(
                activities.start_sandbox_from_snapshot,
                activities.StartFromSnapshotInput(
                    self._provider, self._task_queue_name(), self._suspend_snapshot
                ),
                start_to_close_timeout=DEFAULT_OP_TIMEOUT,
            )
            self._status = out.status
            old_snapshot = self._suspend_snapshot
            self._suspend_snapshot = None
            self._lifecycle = Lifecycle.RUNNING
            # Internal suspend snapshot: delete it after restarting from it.
            try:
                await workflow.execute_activity(
                    activities.delete_snapshot,
                    activities.DeleteSnapshotInput(self._provider, old_snapshot),
                    start_to_close_timeout=DEFAULT_OP_TIMEOUT,
                )
            except Exception as e:
                workflow.logger.error("failed to delete suspend snapshot: %s", e)
            return

        await workflow.execute_activity(
            activities.resume_sandbox,
            activities.ResumeSandboxInput(self._provider, self._status),
            start_to_close_timeout=DEFAULT_OP_TIMEOUT,
        )
        self._lifecycle = Lifecycle.RUNNING

    # ------------------------------------------------------------- teardown

    async def _cleanup(self) -> None:
        if self._provider is None or self._status is None:
            return
        try:
            if self._suspend_snapshot is not None:
                # Sandbox was already stopped by suspend_via_snapshot; just delete
                # the orphaned internal snapshot.
                await workflow.execute_activity(
                    activities.delete_snapshot,
                    activities.DeleteSnapshotInput(self._provider, self._suspend_snapshot),
                    start_to_close_timeout=DEFAULT_OP_TIMEOUT,
                )
            else:
                await workflow.execute_activity(
                    activities.stop_sandbox,
                    activities.StopSandboxInput(self._provider, self._status),
                    start_to_close_timeout=DEFAULT_OP_TIMEOUT,
                )
        except asyncio.CancelledError:
            # Best-effort on parent-close cancellation.
            pass

    # -------------------------------------------------------------- helpers

    def _effective_idle_timeout(self) -> float:
        if self._idle_timeout_seconds > 0:
            return self._idle_timeout_seconds
        return IDLE_AUTO_SUSPEND.total_seconds()

    def _task_queue_name(self) -> str:
        return f"sandbox-{workflow.info().workflow_id}"
