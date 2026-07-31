"""Parent-side sandbox handle and `new_sandbox` (port of `sdk/sandbox.go`).

This is workflow-context code: it runs inside the *parent* (agent) workflow.
`new_sandbox` starts the SandboxWorkflow as a child (workflow ID == sandbox ID),
waits for it to start, then sends the init update via a dispatch activity. Each
method on the returned handle forwards an operation to the sandbox by executing
a dispatch activity with a deterministic, idempotent update ID.
"""

from __future__ import annotations

from datetime import timedelta
from enum import IntEnum
from typing import Optional

from temporalio import workflow

with workflow.unsafe.imports_passed_through():
    from . import dispatch
    from .compute import NO_IDLE_TIMEOUT, CommandResult, ProviderDetails, ProviderSnapshot
    from .workflow import SandboxWorkflow

# Generous timeout: a command may run for a long time inside the sandbox.
DISPATCH_ACTIVITY_TIMEOUT = timedelta(minutes=20)


class CleanupBehavior(IntEnum):
    WITH_WORKFLOW = 0  # cancel the sandbox when the creator workflow closes (default)
    DISABLED = 1  # leave the sandbox running after the creator closes


class Sandbox:
    """A handle to a running sandbox, usable from within the parent workflow."""

    def __init__(self, sandbox_id: str, child_handle) -> None:
        self._id = sandbox_id
        self._handle = child_handle
        self._stopped = False

    @property
    def sandbox_id(self) -> str:
        return self._id

    async def execute_command(
        self, cmd: str, *, disable_auto_resume: bool = False
    ) -> CommandResult:
        return await workflow.execute_activity(
            "send-sandbox-execute-command",
            dispatch.SendExecuteCommandInput(
                self._id, str(workflow.uuid4()), cmd, disable_auto_resume
            ),
            start_to_close_timeout=DISPATCH_ACTIVITY_TIMEOUT,
            result_type=CommandResult,
        )

    async def suspend(self) -> None:
        await workflow.execute_activity(
            "send-sandbox-suspend",
            dispatch.SendSuspendInput(self._id, str(workflow.uuid4())),
            start_to_close_timeout=DISPATCH_ACTIVITY_TIMEOUT,
        )

    async def resume(self) -> None:
        await workflow.execute_activity(
            "send-sandbox-resume",
            dispatch.SendResumeInput(self._id, str(workflow.uuid4())),
            start_to_close_timeout=DISPATCH_ACTIVITY_TIMEOUT,
        )

    async def stop(self) -> None:
        """Signal the sandbox to shut down and block until it fully completes."""
        if self._stopped:
            return
        self._stopped = True
        await self._handle.signal(SandboxWorkflow.stop)
        await self._handle  # wait for the child workflow to complete


async def new_sandbox(
    provider: ProviderDetails,
    *,
    idle_timeout_seconds: float = 0.0,
    cleanup: CleanupBehavior = CleanupBehavior.WITH_WORKFLOW,
    snapshot: Optional[ProviderSnapshot] = None,
) -> Sandbox:
    if idle_timeout_seconds < 0 and idle_timeout_seconds != NO_IDLE_TIMEOUT:
        raise ValueError(
            "idle_timeout_seconds must be >= 0 or NO_IDLE_TIMEOUT, "
            f"got {idle_timeout_seconds}"
        )

    sandbox_id = str(workflow.uuid4())
    parent_close_policy = (
        workflow.ParentClosePolicy.REQUEST_CANCEL
        if cleanup == CleanupBehavior.WITH_WORKFLOW
        else workflow.ParentClosePolicy.ABANDON
    )

    # start_child_workflow returns once the child has started, so the init
    # update below is not sent to a not-yet-running workflow.
    handle = await workflow.start_child_workflow(
        SandboxWorkflow.run,
        workflow.info().workflow_id,
        id=sandbox_id,
        parent_close_policy=parent_close_policy,
    )

    await workflow.execute_activity(
        "send-sandbox-init",
        dispatch.SendInitInput(
            sandbox_id, str(workflow.uuid4()), provider, idle_timeout_seconds, snapshot
        ),
        start_to_close_timeout=DISPATCH_ACTIVITY_TIMEOUT,
    )
    return Sandbox(sandbox_id, handle)
