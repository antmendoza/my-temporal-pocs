"""Worker for the auto-suspend example.

Registers AutoSuspendWorkflow plus the sandbox SDK's workflow/activities on a
single task queue. In-sandbox activities are synchronous (subprocess/shutil) so
a thread-pool executor is supplied; the dispatch activities are async and run on
the event loop.
"""

from __future__ import annotations

import asyncio
from concurrent.futures import ThreadPoolExecutor

from temporalio.client import Client
from temporalio.worker import Worker

from auto_suspend.workflow import TASK_QUEUE, AutoSuspendWorkflow
from sandbox.registration import register


async def main() -> None:
    client = await Client.connect("localhost:7233")
    sandbox_workflows, sandbox_activities = register(client)

    with ThreadPoolExecutor(max_workers=8) as executor:
        worker = Worker(
            client,
            task_queue=TASK_QUEUE,
            workflows=[AutoSuspendWorkflow, *sandbox_workflows],
            activities=sandbox_activities,
            activity_executor=executor,
        )
        print(f"worker running on task queue {TASK_QUEUE!r} (ctrl-c to exit)")
        await worker.run()


if __name__ == "__main__":
    asyncio.run(main())
