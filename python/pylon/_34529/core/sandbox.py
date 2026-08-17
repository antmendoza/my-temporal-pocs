"""Parent-side sandbox handle and `new_sandbox`.

Workflow-context code: runs inside the *parent* workflow. `new_sandbox` starts the
SandboxWorkflow as a child (workflow ID == sandbox ID), waits for it to start, then
sends the init update via a dispatch activity. The handle forwards operations to the
sandbox through dispatch activities with deterministic, idempotent update IDs.
"""

from __future__ import annotations

from datetime import timedelta

from temporalio import workflow

with workflow.unsafe.imports_passed_through():
    from .sandbox_workflow import ExecuteActivityResult, SandboxWorkflow

    from . import dispatch
    from .compute import ProviderDetails

# Generous timeout: the work activity may run for a long time inside the sandbox.
DISPATCH_ACTIVITY_TIMEOUT = timedelta(minutes=20)


class Sandbox:
    """A handle to a running sandbox, usable from within the parent workflow."""

    def __init__(self, sandbox_id: str, child_handle) -> None:
        self._id = sandbox_id
        self._handle = child_handle
        self._stopped = False

    @property
    def sandbox_id(self) -> str:
        return self._id

    async def run_activity(self, sleep_time_seconds: int) -> ExecuteActivityResult:
        return await workflow.execute_activity(
            "send-sandbox-run-activity",
            dispatch.SendExecuteActivityInput(
                self._id, str(workflow.uuid4()), sleep_time_seconds
            ),
            start_to_close_timeout=DISPATCH_ACTIVITY_TIMEOUT,
            result_type=ExecuteActivityResult,
        )

    async def stop(self) -> None:
        """Signal the sandbox to shut down and block until it fully completes."""
        if self._stopped:
            return
        self._stopped = True
        await self._handle.signal(SandboxWorkflow.stop)
        await self._handle  # wait for the child workflow to complete


async def new_sandbox(provider: ProviderDetails, *, session_ref: str = "") -> Sandbox:
    sandbox_id = str(workflow.uuid4())

    # start_child_workflow returns once the child has started, so the init update
    # below is not sent to a not-yet-running workflow.
    handle = await workflow.start_child_workflow(
        SandboxWorkflow.run,
        workflow.info().workflow_id,
        id=sandbox_id,
        parent_close_policy=workflow.ParentClosePolicy.REQUEST_CANCEL,
    )

    await workflow.execute_activity(
        "send-sandbox-init",
        dispatch.SendInitInput(sandbox_id, str(workflow.uuid4()), provider, session_ref),
        start_to_close_timeout=DISPATCH_ACTIVITY_TIMEOUT,
    )
    return Sandbox(sandbox_id, handle)
