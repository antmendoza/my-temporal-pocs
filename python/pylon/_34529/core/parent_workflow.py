"""ParentRecreateWorkflow.

When the child timeout the error propagates out of the child and surfaces in this parent,
which tears the dead sandbox down and creates a
*brand-new* one (new child workflow, new `sandbox-<id>` task queue, new micro-VM).

Progress still survives because the activity checkpoints under a durable,
VM-independent `session_ref` (stand-in for BYO S3/EFS): each new sandbox is created
for the same session, so its in-VM worker resumes from the last checkpoint. This
mocks AgentCore, where a re-provision is a genuinely new micro-VM and state
comes from the durable mount, not the VM.
"""

from __future__ import annotations

from temporalio import workflow
from temporalio.exceptions import ActivityError, ApplicationError

with workflow.unsafe.imports_passed_through():
    from .compute import SANDBOX_WORKER_LOST, ProviderDetails
    from .providers.mockAgentCore import PROVIDER_MOCK_AGENTCORE
    from .sandbox import new_sandbox

TASK_QUEUE = "parent-recreate-queue"

# Work outlasts the micro-VM lifetime, forcing the parent to recreate the sandbox.
WORK_SECONDS = 20
VM_MAX_LIFETIME_SECONDS = 8
MAX_SANDBOXES = 6


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
