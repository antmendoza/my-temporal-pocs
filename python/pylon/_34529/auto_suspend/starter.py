"""Starts the auto-suspend example workflow and prints the before/after listing."""

from __future__ import annotations

import asyncio

from temporalio.client import Client

from auto_suspend.workflow import TASK_QUEUE, AutoSuspendWorkflow


async def main() -> None:
    client = await Client.connect("localhost:7233")
    result = await client.execute_workflow(
        AutoSuspendWorkflow.run,
        id="auto-suspend-example",
        task_queue=TASK_QUEUE,
    )
    print("before suspend:", result.before_suspend.strip())
    print("after  suspend:", result.after_suspend.strip())


if __name__ == "__main__":
    asyncio.run(main())
