import asyncio
import logging

from temporalio.client import Client

import  workflows


async def main():
    logging.basicConfig(level=logging.INFO)

    # Connect client
    client = await Client.connect(
        "localhost:7233",
    )

    # Start workflow, send signal, wait for completion, issue query
    handle = await client.start_workflow(
        workflows.ExecutorRestrictedWorkflow.run,
        "Temporal",
        id=f"context-propagation-workflow-id",
        task_queue="context-propagation-task-queue",
    )


    result = await handle.result()
    logging.info(f"Workflow result: {result}")


if __name__ == "__main__":
    asyncio.run(main())
