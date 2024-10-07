import asyncio

from temporalio import common, workflow
from temporalio.client import Client
from temporalio.worker import Worker


@workflow.defn
class StackTraceBugRepro:

    def __init__(self) -> None:
        self.val = 1

    async def wait_until_positive(self):
        while True:
            print("wait_until_positive")
            await workflow.wait_condition(lambda: self.val > 0)
            self.val = -self.val

    async def wait_until_negative(self):
        while True:
            print("wait_until_negative")
            await workflow.wait_condition(lambda: self.val < 0)
            self.val = -self.val

    @workflow.run
    async def run(self):
        await asyncio.gather(self.wait_until_negative(), self.wait_until_positive())


async def main():
    client = await Client.connect("localhost:7233")
    async with Worker(
            client,
            task_queue="tq",
            workflows=[StackTraceBugRepro],
            # max_cached_workflows=1,
            # max_concurrent_workflow_tasks=1

    ):
        await client.execute_workflow(
            StackTraceBugRepro.run,
            id="wid",
            task_queue="tq",
            id_reuse_policy=common.WorkflowIDReusePolicy.TERMINATE_IF_RUNNING,
        )


if __name__ == "__main__":
    asyncio.run(main())
