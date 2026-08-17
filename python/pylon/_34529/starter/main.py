"""Starts the parent-recreate example workflow and prints the activity result."""

from __future__ import annotations

import asyncio

from temporalio.client import Client

from core.parent_workflow import TASK_QUEUE, ParentRecreateWorkflow


async def main() -> None:
    client = await Client.connect("localhost:7233")
    result = await client.execute_workflow(
        ParentRecreateWorkflow.run,
        id="parent-recreate-example",
        task_queue=TASK_QUEUE,
    )
    print("result:", result)


if __name__ == "__main__":
    asyncio.run(main())
