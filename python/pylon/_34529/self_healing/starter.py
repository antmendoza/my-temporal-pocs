"""Starts the self-healing example workflow and prints the activity result."""

from __future__ import annotations

import asyncio

from temporalio.client import Client

from self_healing.workflow import TASK_QUEUE, SelfHealingWorkflow


async def main() -> None:
    client = await Client.connect("localhost:7233")
    result = await client.execute_workflow(
        SelfHealingWorkflow.run,
        id="self-healing-example",
        task_queue=TASK_QUEUE,
    )
    print("result:", result)


if __name__ == "__main__":
    asyncio.run(main())
