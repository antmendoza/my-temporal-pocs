"""Always-on worker for the parent-recreate (replace-and-restore) example.

Runs ParentRecreateWorkflow plus the sandbox_replace SDK's workflow/activities on a
fixed task queue. This worker drives provisioning (start-sandbox / stop-sandbox),
which boots and evicts the ephemeral in-VM workers on `sandbox-<id>`.
"""

from __future__ import annotations

import asyncio
from concurrent.futures import ThreadPoolExecutor

from temporalio.client import Client
from temporalio.worker import Worker

from parent_recreate.workflow import TASK_QUEUE, ParentRecreateWorkflow
from sandbox_replace.registration import register


async def main() -> None:
    client = await Client.connect("localhost:7233")
    sandbox_workflows, sandbox_activities = register(client)

    with ThreadPoolExecutor(max_workers=8) as executor:
        worker = Worker(
            client,
            task_queue=TASK_QUEUE,
            workflows=[ParentRecreateWorkflow, *sandbox_workflows],
            activities=sandbox_activities,
            activity_executor=executor,
        )
        print(f"worker running on task queue {TASK_QUEUE!r} (ctrl-c to exit)")
        await worker.run()


if __name__ == "__main__":
    asyncio.run(main())
