import asyncio
from dataclasses import dataclass
from datetime import timedelta

from temporalio import activity, workflow
from temporalio.client import Client

from temporalio.worker import Worker

from hello_10.test_types import ComposeGreetingInput


#pydantic_data_converter = DataConverter(
#    payload_converter_class=PydanticPayloadConverter
#)  # pydantic_data_converter: instance to be harnessed by the client


#@dataclass
#class ComposeGreetingInput:
#    greeting: str
#    name: str


# Basic activity that logs and does string concatenation
@activity.defn
async def compose_greeting(input_data: ComposeGreetingInput) -> str:
    activity.logger.info(f"Running activity with parameter {input_data}")
    return f"{input_data.greeting}, {input_data.name}!"


# Basic workflow that logs and invokes an activity
@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        workflow.logger.info(f"Running workflow with parameter {name}")
        seconds_ = await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=100),
        )

        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=100),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=100),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=100),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=100),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=100),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=100),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=100),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=100),
        )

        return seconds_


async def main():
    # Uncomment the lines below to see logging output
    # import logging
    # logging.basicConfig(level=logging.INFO)

    # Start client
    client = await Client.connect("localhost:7233")

    # Run a worker for the workflow
    async with Worker(
            client,
            task_queue="hello-activity-task-queue",
            workflows=[GreetingWorkflow],
            activities=[compose_greeting],
            max_concurrent_workflow_tasks=200,
    ):
        # Create a list of 1000 coroutines
        tasks = [asyncio.create_task(method_name(client, i)) for i in range(2)]

        # Wait for all tasks to complete
        await asyncio.gather(*tasks)


async def method_name(client, i):
    # While the worker is running, use the client to run the workflow and
    # print out its result. Note, in many production setups, the client
    # would be in a completely separate process from the worker.
    result = await client.execute_workflow(
        GreetingWorkflow.run,
        "World",
        id="hello-activity-workflow-idwsw" + str(i),
        task_queue="hello-activity-task-queue",
        task_timeout=timedelta(seconds=125)
    )
    print(f"Result: {result}")


if __name__ == "__main__":
    asyncio.run(main())
