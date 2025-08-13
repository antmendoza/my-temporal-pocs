import asyncio

from temporalio.client import Client
from temporalio.common import WorkflowIDReusePolicy

from workflow import GreetingWorkflow, Input


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
    workflowId = "hello"
    handle = await client.start_workflow(
        GreetingWorkflow.run,
        Input(immediate_runs=0, current_run=0),
        id=workflowId,
        task_queue="hello-activity-task-queue",
        id_reuse_policy=WorkflowIDReusePolicy.TERMINATE_IF_RUNNING
    )
    print(f"completed workflow id : {workflowId}")

    await handle.signal("add_immediate_run")
    await handle.signal("add_immediate_run")
    await handle.signal("add_immediate_run")
    await handle.signal("add_immediate_run")

    await handle.result()
if __name__ == "__main__":
    asyncio.run(main())
