import asyncio
import concurrent
import time
from concurrent.futures.thread import ThreadPoolExecutor
from dataclasses import dataclass
from datetime import timedelta

from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.common import RetryPolicy, WorkflowIDReusePolicy
from temporalio.worker import Worker


@dataclass
class ComposeGreetingInput:
    greeting: str
    name: str


@dataclass
class MyInput:
    recomputation_date_iso: str
    name: str


@activity.defn
async def async_compose_greeting(input: ComposeGreetingInput) -> str:
    print("Running activity async_compose_greeting parameter %s" % input)

    time.sleep(10)

    return f"{input.greeting}, {input.name}!"


@activity.defn
def def_compose_greeting(input: ComposeGreetingInput) -> str:
    print("Running activity def_compose_greeting parameter %s" % input)

    #time.sleep(2)

    print("hola...heartbeat")

    while True:
        activity.heartbeat("...")

    return ""


@activity.defn
def def_compose_greeting_2(input: ComposeGreetingInput) -> str:
    print("Running activity def_compose_greeting_2 parameter %s" % input)

    time.sleep(12)

    return ""

# return f"{input.greeting}, {input.name}!"


# Basic workflow that logs and invokes an activity
class QueryRegistry:
    query_1 = "query_1"
    query_2 = "query_2"


@workflow.defn
class GreetingWorkflow:
    def __init__(self):
        self.some_date = None
        self.name = None

    @workflow.run
    async def run(self, input: MyInput) -> str:

        try:


            asyncio.create_task(
                workflow.execute_activity(
                    def_compose_greeting,
                    ComposeGreetingInput("Hello", input.name),
                    start_to_close_timeout=timedelta(seconds=400),
                    retry_policy=RetryPolicy(
                        maximum_attempts=5,
                    ),
                    heartbeat_timeout=timedelta(seconds=11)
                )

            )

            await asyncio.sleep(0.5)



            await workflow.execute_activity(
                async_compose_greeting,
                ComposeGreetingInput("Hello", input.name),
                start_to_close_timeout=timedelta(seconds=400),
                retry_policy=RetryPolicy(
                    maximum_attempts=1,
                )
            )



        except Exception as e:

            raise e

        return ""


async def main():
    client = await Client.connect("localhost:7233")

    # Run a worker for the workflow
    async with Worker(
            client,
            task_queue="hello-mtls-task-queue",
            workflows=[GreetingWorkflow],
            activities=[async_compose_greeting,
                        def_compose_greeting,
                        def_compose_greeting_2],
            activity_executor=ThreadPoolExecutor(10)

    ):
        result = await client.start_workflow(
            GreetingWorkflow.run,
            MyInput(recomputation_date_iso="test", name="test2"),
            id="hello-mtls-workflow-id",
            task_queue="hello-mtls-task-queue",
            id_reuse_policy=WorkflowIDReusePolicy.TERMINATE_IF_RUNNING
        )


        await asyncio.sleep(1)


        print("hola sleep 30")
        #time.sleep(30)


        print(f"Result: { await result.result()}")


if __name__ == "__main__":
    asyncio.run(main())
