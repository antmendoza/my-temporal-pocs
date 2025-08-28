import asyncio
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass
from datetime import timedelta

from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.worker import Worker


@dataclass
class ComposeGreetingInput:
    greeting: str
    name: str


@activity.defn
def compose_greeting(input: ComposeGreetingInput) -> str:
    activity.logger.info("Running activity with parameter %s" % input)
    return f"{input.greeting}, {input.name}!"


# Basic workflow that logs and invokes an activity
@workflow.defn(sandboxed=False)
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        workflow.logger.info("Running workflow with parameter %s" % name)
        activity_ = workflow.execute_activity(compose_greeting, ComposeGreetingInput("Hello", name),
                                             start_to_close_timeout=timedelta(seconds=10), )

        activity_result = await activity_
        
        
        print(f"Activity result: {activity_result}")

        

        await workflow.sleep(1)


        print(f"Activity result: {activity_result}")


        return activity_result

async def main():

    client = await Client.connect("localhost:7233")

    worker = Worker(
        client,
        task_queue="hello-activity-task-queue",
        workflows=[GreetingWorkflow],
        activities=[compose_greeting],
        activity_executor=ThreadPoolExecutor(5),
        debug_mode=True,
    )

    await worker.run()


if __name__ == "__main__":
    asyncio.run(main())
