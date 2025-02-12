import asyncio
import concurrent
import logging
import os

from temporalio.client import Client
from temporalio.runtime import Runtime, TelemetryConfig
from temporalio.worker import Worker


from activity import compose_greeting
from workflow import GreetingWorkflow
from test_types import ComposeGreetingRequest

from converter import pydantic_data_converter


async def main():
    # Uncomment the lines below to see logging output
    # import logging

#    os.environ["RUST_LOG"] = "temporal_sdk_core=DEBUG"

#    logging.basicConfig(level=logging.DEBUG)


    # Start client
    client = await Client.connect(
        "localhost:7233",
        data_converter=pydantic_data_converter)



    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as activity_executor:
        worker = Worker(
            client,
            task_queue="hello-activity-task-queue",
            workflows=[GreetingWorkflow],
            activities=[compose_greeting],
            max_concurrent_workflow_tasks=200,
            max_concurrent_activities=4,
            activity_executor=activity_executor,
        )
        tasks = [asyncio.create_task(start_workflow(client, i))
                 for i in range(1)]

        await worker.run()


async def start_workflow(client, i):
    # While the worker is running, use the client to run the workflow and
    # print out its result. Note, in many production setups, the client
    # would be in a completely separate process from the worker.
    result = await client.execute_workflow(
        GreetingWorkflow.run,
        ComposeGreetingRequest(id=str(i), name="World"),
        id="hello-activity-workflow-id-" + str(i),
        task_queue="hello-activity-task-queue",
        # task_timeout=timedelta(seconds=120)
    )
    print(f"Result: {result}")


if __name__ == "__main__":
    asyncio.run(main())
