import asyncio
import concurrent

from temporalio.client import Client
from temporalio.runtime import Runtime, TelemetryConfig, PrometheusConfig
from temporalio.worker import Worker
from temporalio.common import WorkflowIDReusePolicy


from activity import compose_greeting
from workflow import GreetingWorkflow
from test_types import ComposeGreetingRequest

from converter import pydantic_data_converter





def init_runtime_with_prometheus(port: int) -> Runtime:
    # Create runtime for use with Prometheus metrics
    return Runtime(
        telemetry=TelemetryConfig(
            metrics=PrometheusConfig(
                bind_address=f"127.0.0.1:{port}",
            )
        )
    )

async def main():
    # Uncomment the lines below to see logging output
    # import logging

#    os.environ["RUST_LOG"] = "temporal_sdk_core=DEBUG"

#    logging.basicConfig(level=logging.DEBUG)

    runtime = init_runtime_with_prometheus(8086)


    # Start client
    client = await Client.connect(
        "localhost:7233",
        runtime=runtime,
        data_converter=pydantic_data_converter)



    with concurrent.futures.ThreadPoolExecutor(max_workers=400) as activity_executor:
        worker = Worker(
            client,
            task_queue="hello-activity-task-queue",
            workflows=[GreetingWorkflow],
            activities=[compose_greeting],
            #max_cached_workflows=4,
            #max_concurrent_workflow_tasks=10,
            max_concurrent_activities=400,
            activity_executor=activity_executor,
        )
        tasks = [asyncio.create_task(start_workflow(client, i))
                 for i in range(20)]

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
        id_conflict_policy=WorkflowIDReusePolicy.ALLOW_DUPLICATE
    )
    print(f"Result: {result}")


if __name__ == "__main__":
    asyncio.run(main())
