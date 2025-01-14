import asyncio
from dataclasses import dataclass
from datetime import timedelta

from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.worker import Worker

from hello_10.test_types import ComposeGreetingInput


# Basic activity that logs and does string concatenation
@activity.defn
async def compose_greeting(input: ComposeGreetingInput) -> str:
    activity.logger.info("Running activity with parameter %s" % input)
    return f"{input.greeting}, {input.name}!"


# Basic workflow that logs and invokes an activity
@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        workflow.logger.info("Running workflow with parameter %s" % name)
        seconds_ = await workflow.execute_activity(compose_greeting, ComposeGreetingInput("Hello", name),
                                                   start_to_close_timeout=timedelta(seconds=100), )

        await workflow.execute_activity(compose_greeting, ComposeGreetingInput("Hello", name),
                                        start_to_close_timeout=timedelta(seconds=100), )
        await workflow.execute_activity(compose_greeting, ComposeGreetingInput("Hello", name),
                                        start_to_close_timeout=timedelta(seconds=100), )
        await workflow.execute_activity(compose_greeting, ComposeGreetingInput("Hello", name),
                                        start_to_close_timeout=timedelta(seconds=100), )
        await workflow.execute_activity(compose_greeting, ComposeGreetingInput("Hello", name),
                                        start_to_close_timeout=timedelta(seconds=100), )
        await workflow.execute_activity(compose_greeting, ComposeGreetingInput("Hello", name),
                                        start_to_close_timeout=timedelta(seconds=100), )
        await workflow.execute_activity(compose_greeting, ComposeGreetingInput("Hello", name),
                                        start_to_close_timeout=timedelta(seconds=100), )
        await workflow.execute_activity(compose_greeting, ComposeGreetingInput("Hello", name),
                                        start_to_close_timeout=timedelta(seconds=100), )
        await workflow.execute_activity(compose_greeting, ComposeGreetingInput("Hello", name),
                                        start_to_close_timeout=timedelta(seconds=100), )

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
    ):
        # Create a list of 2000 coroutines
        tasks = [asyncio.create_task(method_name(client, i)) for i in range(2000)]

        # Wait for all tasks to complete
        await asyncio.gather(*tasks)


async def method_name(client, i):
    # While the worker is running, use the client to run the workflow and
    # print out its result. Note, in many production setups, the client
    # would be in a completely separate process from the worker.
    result = await client.execute_workflow(
        GreetingWorkflow.run,
        "World",
        id="hello-activity-workflow-id" + str(i),
        task_queue="hello-activity-task-queue",
    )
    print(f"Result: {result}")


if __name__ == "__main__":
    asyncio.run(main())
