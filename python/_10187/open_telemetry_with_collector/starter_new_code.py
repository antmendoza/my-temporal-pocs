import asyncio
import os

from temporalio.client import Client
from temporalio.contrib.opentelemetry import TracingInterceptor

from open_telemetry_with_collector.worker import init_runtime_with_telemetry, GreetingWorkflow


async def create_workflow(client, x):
    # Run workflow
    result = await client.start_workflow(
        GreetingWorkflow.run,
        "Temporal",
        id=f"open_telemetry-workflow-id"+ str(x),
        task_queue="open_telemetry-task-queue",
    )

    print(f"Workflow started: {result.id}")



async def main():
    runtime = init_runtime_with_telemetry()

    # Connect client
    client = await Client.connect(
        "localhost:7233",
        # Use OpenTelemetry interceptor
        interceptors=[TracingInterceptor()],
        runtime=runtime,
    )


    # This is the new code,
    workflow_count = int(os.environ.get("WORKFLOW_COUNT"))
    tasks = [create_workflow(client, x) for x in range(workflow_count)]
    await asyncio.gather(*tasks)


if __name__ == "__main__":
    asyncio.run(main())
