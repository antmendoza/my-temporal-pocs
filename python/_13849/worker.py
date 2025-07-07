import asyncio
import logging

from temporalio.client import Client
from temporalio.worker import Worker

import activities, workflows

interrupt_event = asyncio.Event()


async def main():
    logging.basicConfig(level=logging.INFO)

    # Connect client
    client = await Client.connect(
        "localhost:7233",
    )

    # Run a worker for the workflow
    async with Worker(
        client,
        task_queue="context-propagation-task-queue",
        activities=[activities.say_hello_activity],
        workflows=[workflows.ExecutorRestrictedWorkflow],
    ):
        # Wait until interrupted
        logging.info("Worker started, ctrl+c to exit")
        await asyncio.sleep(400)



if __name__ == "__main__":
    asyncio.run(main())
