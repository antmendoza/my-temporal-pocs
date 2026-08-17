"""ParentRecreateWorkflow and the in-workflow sandbox handle.

When the child timeout the error propagates out of the child and surfaces in this parent,
which tears the dead sandbox down and creates a
*brand-new* one (new child workflow, new `sandbox-<id>` task queue, new micro-VM).

Progress still survives because the activity checkpoints under a durable,
VM-independent `session_ref` (stand-in for BYO S3/EFS): each new sandbox is created
for the same session, so its in-VM worker resumes from the last checkpoint. This
mocks AgentCore, where a re-provision is a genuinely new micro-VM and state
comes from the durable mount, not the VM.

`Sandbox` / `new_sandbox` are workflow-context helpers: `new_sandbox` starts the
SandboxWorkflow as a child (workflow ID == sandbox ID), waits for it to start, then
sends the init update via a dispatch activity. The handle forwards operations to the
sandbox through dispatch activities with deterministic, idempotent update IDs.
"""

from __future__ import annotations

from datetime import timedelta

from temporalio import workflow
from temporalio.exceptions import ActivityError, ApplicationError

with workflow.unsafe.imports_passed_through():
    from . import dispatch
    from .compute import SANDBOX_WORKER_LOST, ProviderDetails
    from .providers.mockAgentCore import PROVIDER_MOCK_AGENTCORE
    from .sandbox_workflow import ExecuteActivityResult, SandboxWorkflow

TASK_QUEUE = "parent-recreate-queue"

# Work outlasts the micro-VM lifetime, forcing the parent to recreate the sandbox.
WORK_SECONDS = 20
VM_MAX_LIFETIME_SECONDS = 8
MAX_SANDBOXES = 6

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


@workflow.defn
class ParentRecreateWorkflow:
    @workflow.run
    async def run(self) -> str:
        session_ref = str(workflow.uuid4())
        provider = ProviderDetails(
            type=PROVIDER_MOCK_AGENTCORE,
            config={
                "image": "ubuntu:26.04",
                "max_lifetime": str(VM_MAX_LIFETIME_SECONDS),
            },
        )
        for attempt in range(1, MAX_SANDBOXES + 1):
            sbx = await new_sandbox(provider, session_ref=session_ref)
            try:
                result = await sbx.run_activity(WORK_SECONDS)
                await sbx.stop()
                return f"{result.stdout} [completed with {attempt} sandbox(es)]"
            except ActivityError as e:
                cause = e.cause
                if not (
                    isinstance(cause, ApplicationError)
                    and cause.type == SANDBOX_WORKER_LOST
                ):
                    raise  # not a VM loss -> surface the real error, don't recreate
                workflow.logger.warning(
                    "sandbox %d lost (%s); tearing it down and recreating",
                    attempt,
                    cause,
                )
                await sbx.stop()
        raise ApplicationError(
            f"work did not complete within {MAX_SANDBOXES} sandboxes",
            type="MaxSandboxesExceeded",
        )
