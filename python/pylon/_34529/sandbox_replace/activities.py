"""In-sandbox lifecycle activities (port of `sdk/workflow/activities.go`).

Each activity looks up the provider and calls one of its methods. They run in
the worker's activity thread pool (sync `def`), so provider I/O (subprocess,
shutil, future boto3 calls) is fine here. `UnsupportedOperation` is converted to
a non-retryable ApplicationError of type "ErrUnsupported" so the workflow can
detect it and fall back to snapshot-based suspend.
"""

from __future__ import annotations

import os
import tempfile
import time
from dataclasses import dataclass
from typing import Optional

from temporalio import activity
from temporalio.exceptions import ApplicationError


def _session_base() -> str:
    # Durable, VM-independent checkpoint store (stand-in for BYO S3/EFS), keyed by
    # the parent-supplied session_ref so it survives sandbox teardown/recreation.
    # Resolved lazily inside activities so no restricted call runs at import time
    # (this module is pulled into workflow-sandbox validation).
    return os.path.join(tempfile.gettempdir(), "sandbox-harness", "sessions")

from . import compute
from .compute import (
    CommandResult,
    ProviderDetails,
    ProviderSnapshot,
    ProviderStatus,
    SandboxPostSnapshotState,
    UnsupportedOperation,
)

# Import providers so they self-register via register_provider() at import time.
from .providers import agentcore as _agentcore  # noqa: F401
from .providers import local as _local  # noqa: F401
from .providers import mockAgentCore as _mock_agentcore  # noqa: F401


def _lookup(pd: ProviderDetails):
    try:
        return compute.lookup(pd.type, pd.config)
    except Exception as e:  # provider config error → non-retryable
        raise ApplicationError(str(e), type="ProviderConfigError", non_retryable=True)


def _guard_unsupported(e: Exception) -> ApplicationError:
    return ApplicationError(str(e), type="ErrUnsupported", non_retryable=True)


@dataclass
class StartSandboxInput:
    provider: ProviderDetails
    task_queue_name: str


@dataclass
class StartSandboxOutput:
    status: ProviderStatus


@dataclass
class StopSandboxInput:
    provider: ProviderDetails
    status: ProviderStatus


@dataclass
class SuspendSandboxInput:
    provider: ProviderDetails
    status: ProviderStatus


@dataclass
class ResumeSandboxInput:
    provider: ProviderDetails
    status: ProviderStatus


@dataclass
class ExecuteCommandActivityInput:
    provider: ProviderDetails
    status: ProviderStatus
    command: str


@dataclass
class ActivityInSandboxInput:
    sleepTimeSeconds: int
    # Durable, VM-independent session key. Resolved to a checkpoint dir that
    # survives sandbox teardown, so a fresh sandbox for the same session resumes.
    session_ref: str


@dataclass
class ActivityInSandboxResult:
    result: str



@dataclass
class ExecuteCommandActivityOutput:
    result: CommandResult


@dataclass
class SnapshotSandboxInput:
    provider: ProviderDetails
    status: ProviderStatus


@dataclass
class SnapshotSandboxOutput:
    sandbox_state: int  # SandboxPostSnapshotState
    snapshot: Optional[ProviderSnapshot]


@dataclass
class StartFromSnapshotInput:
    provider: ProviderDetails
    task_queue_name: str
    snapshot: ProviderSnapshot


@dataclass
class StartFromSnapshotOutput:
    status: ProviderStatus


@dataclass
class DeleteSnapshotInput:
    provider: ProviderDetails
    snapshot: ProviderSnapshot


@activity.defn(name="start-sandbox")
def start_sandbox(inp: StartSandboxInput) -> StartSandboxOutput:
    provider = _lookup(inp.provider)
    try:
        status = provider.start(inp.task_queue_name)
    except UnsupportedOperation as e:
        raise _guard_unsupported(e)
    return StartSandboxOutput(status=status)


@activity.defn(name="stop-sandbox")
def stop_sandbox(inp: StopSandboxInput) -> None:
    provider = _lookup(inp.provider)
    try:
        provider.stop(inp.status)
    except UnsupportedOperation as e:
        raise _guard_unsupported(e)


@activity.defn(name="suspend-sandbox")
def suspend_sandbox(inp: SuspendSandboxInput) -> None:
    provider = _lookup(inp.provider)
    try:
        provider.suspend(inp.status)
    except UnsupportedOperation as e:
        raise _guard_unsupported(e)


@activity.defn(name="resume-sandbox")
def resume_sandbox(inp: ResumeSandboxInput) -> None:
    provider = _lookup(inp.provider)
    try:
        provider.resume(inp.status)
    except UnsupportedOperation as e:
        raise _guard_unsupported(e)


@activity.defn(name="execute-command")
def execute_command(inp: ExecuteCommandActivityInput) -> ExecuteCommandActivityOutput:
    provider = _lookup(inp.provider)
    try:
        result = provider.execute_command(inp.status, inp.command)
    except UnsupportedOperation as e:
        raise _guard_unsupported(e)
    return ExecuteCommandActivityOutput(result=result)




@activity.defn(name="execute-activity-in-sandbox")
def execute_activity_in_sandbox(inp: ActivityInSandboxInput) -> ActivityInSandboxResult:
    # Scheduled on the sandbox's own task queue -> served by the in-VM worker the
    # provider booted. Heartbeats for liveness (so a dead worker is detected via
    # heartbeat_timeout and the failure propagates to the parent), and checkpoints
    # elapsed seconds to the DURABLE session store keyed by session_ref. Because
    # that store is independent of any single sandbox, the fresh sandbox the parent
    # creates after a failure resumes from here instead of restarting from zero.
    info = activity.info()
    durable_dir = os.path.join(_session_base(), inp.session_ref)
    os.makedirs(durable_dir, exist_ok=True)
    progress_file = os.path.join(durable_dir, "progress")
    try:
        with open(progress_file) as f:
            elapsed = int(f.read().strip() or "0")
    except FileNotFoundError:
        elapsed = 0
    resumed_from = elapsed
    while elapsed < inp.sleepTimeSeconds:
        time.sleep(1)
        elapsed += 1
        with open(progress_file, "w") as f:
            f.write(str(elapsed))
        activity.heartbeat(elapsed)
        print(f"progress {elapsed}/{inp.sleepTimeSeconds}s on task queue {info.task_queue}")
    return ActivityInSandboxResult(
        result=(
            f"completed {inp.sleepTimeSeconds}s of work on task queue "
            f"{info.task_queue} (resumed from {resumed_from}s)"
        )
    )




@activity.defn(name="snapshot-sandbox")
def snapshot_sandbox(inp: SnapshotSandboxInput) -> SnapshotSandboxOutput:
    provider = _lookup(inp.provider)
    try:
        state, snapshot = provider.snapshot(inp.status)
    except UnsupportedOperation as e:
        raise _guard_unsupported(e)
    return SnapshotSandboxOutput(sandbox_state=int(state), snapshot=snapshot)


@activity.defn(name="start-sandbox-from-snapshot")
def start_sandbox_from_snapshot(inp: StartFromSnapshotInput) -> StartFromSnapshotOutput:
    provider = _lookup(inp.provider)
    try:
        status = provider.start_from_snapshot(inp.task_queue_name, inp.snapshot)
    except UnsupportedOperation as e:
        raise _guard_unsupported(e)
    return StartFromSnapshotOutput(status=status)


@activity.defn(name="delete-snapshot")
def delete_snapshot(inp: DeleteSnapshotInput) -> None:
    provider = _lookup(inp.provider)
    try:
        provider.delete_snapshot(inp.snapshot)
    except UnsupportedOperation as e:
        raise _guard_unsupported(e)
