"""AutoSuspendWorkflow — Python port of examples/auto-suspend/workflow.go.

Creates a sandbox, writes a file, waits past the idle timeout (so the sandbox
auto-suspends via snapshot), then verifies the file is still present after the
next command transparently resumes it.
"""

from __future__ import annotations

import asyncio
from dataclasses import dataclass

from temporalio import workflow

with workflow.unsafe.imports_passed_through():
    from sandbox.compute import PROVIDER_LOCAL, ProviderDetails
    from sandbox.providers.mockAgentCore import PROVIDER_MOCK_AGENTCORE
    from sandbox.sandbox import new_sandbox

TASK_QUEUE = "auto-suspend-queue"

# Idle timeout is 30s; wait 45s so the sandbox auto-suspends during the wait.
IDLE_TIMEOUT_SECONDS = 30.0
WAIT_SECONDS = 45.0


@dataclass
class WorkflowResult:
    before_suspend: str
    after_suspend: str


@workflow.defn
class AutoSuspendWorkflow:
    @workflow.run
    async def run(self) -> WorkflowResult:
        sbx = await new_sandbox(
            ProviderDetails(type=PROVIDER_MOCK_AGENTCORE, config={"image": "ubuntu:26.04"}),
            idle_timeout_seconds=IDLE_TIMEOUT_SECONDS,
        )
        # sbx = await new_sandbox(
        #     ProviderDetails(type=PROVIDER_LOCAL, config={"image": "ubuntu:26.04"}),
        #     idle_timeout_seconds=IDLE_TIMEOUT_SECONDS,
        # )

        # Runs on the sandbox's own task queue -> served by the in-VM worker.
        act = await sbx.run_activity(3)
        workflow.logger.info("ran in-VM activity: %s", act.stdout)

        # Runs on the sandbox's own task queue -> served by the in-VM worker.
        act = await sbx.run_activity(20)
        workflow.logger.info("ran in-VM activity: %s", act.stdout)


        await sbx.execute_command("mkdir -p session")
        await sbx.execute_command("touch session/persist.txt")
        before = await sbx.execute_command("ls -la session/persist.txt")

        # The sandbox auto-suspends (snapshot + stop) during this wait.
        await asyncio.sleep(WAIT_SECONDS)

        # This command resumes the sandbox from its snapshot first.
        after = await sbx.execute_command("ls -la session/persist.txt")

        await sbx.stop()
        return WorkflowResult(before_suspend=before.stdout, after_suspend=after.stdout)
