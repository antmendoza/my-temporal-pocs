"""SelfHealingWorkflow — an in-VM activity that survives repeated micro-VM evictions.

The sandbox is provisioned with a short `max_lifetime`, so the mock AgentCore
provider evicts the in-VM worker mid-activity (the stand-in for AgentCore's hard
8h max-lifetime). The SandboxWorkflow detects the loss via heartbeat_timeout,
re-provisions a fresh worker, and the activity resumes from its checkpoint in
session storage — so `WORK_SECONDS` of work completes even though no single
micro-VM lives that long.
"""

from __future__ import annotations

from temporalio import workflow

with workflow.unsafe.imports_passed_through():
    from sandbox.compute import NO_IDLE_TIMEOUT, ProviderDetails
    from sandbox.providers.mockAgentCore import PROVIDER_MOCK_AGENTCORE
    from sandbox.sandbox import new_sandbox

TASK_QUEUE = "self-healing-queue"

# Work outlasts the micro-VM lifetime, forcing at least two re-provisions.
WORK_SECONDS = 20
VM_MAX_LIFETIME_SECONDS = 8


@workflow.defn
class SelfHealingWorkflow:
    @workflow.run
    async def run(self) -> str:
        sbx = await new_sandbox(
            ProviderDetails(
                type=PROVIDER_MOCK_AGENTCORE,
                config={
                    "image": "ubuntu:26.04",
                    "max_lifetime": str(VM_MAX_LIFETIME_SECONDS),
                },
            ),
            # Don't auto-suspend; we want the max-lifetime eviction to be the only
            # thing that kills the worker during the demo.
            idle_timeout_seconds=NO_IDLE_TIMEOUT,
        )

        await sbx.run_activity(2)

        result = await sbx.run_activity(WORK_SECONDS)
        await sbx.stop()
        return result.stdout
