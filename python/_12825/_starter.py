import asyncio

from temporalio.client import Client
from temporalio.common import WorkflowIDReusePolicy

from interceptor import ActivityRetryInterceptor
from workflow import GreetingWorkflow


async def main():
    # runtime = init_runtime_with_prometheus(8086)

    # Start client
    client = await Client.connect(
        "localhost:7233",
    )

    tasks = []
    for i in range(1):
        tasks.append(asyncio.create_task(start_workflow(client, i)))

    #    tasks = [asyncio.create_task(start_workflow(client, i))
    #         for i in range(100)]

    await  asyncio.gather(*tasks)


async def start_workflow(client, i):
    workflowId = "hello-activity-workflow-id-" + str(i)
    result = await client.execute_workflow(
        GreetingWorkflow.run,
        "World_"+str(i),
        id=workflowId,
        task_queue="hello-activity-task-queue",
        # task_timeout=timedelta(seconds=120)
        id_conflict_policy=WorkflowIDReusePolicy.TERMINATE_IF_RUNNING
    )
    print(f"completed workflow id : {workflowId}")


if __name__ == "__main__":
    asyncio.run(main())
