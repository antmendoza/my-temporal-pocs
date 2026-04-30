import asyncio
import os
import time
import uuid

from temporalio.client import Client
from temporalio.contrib.opentelemetry import TracingInterceptor

from open_telemetry_with_collector.worker import init_runtime_with_telemetry, GreetingWorkflow


async def create_workflow(client, x):
    # Run workflow
    result = await client.start_workflow(
        GreetingWorkflow.run,
        "Temporal",
        id=f"open_telemetry-workflow-id"+ str(x) + str(uuid.uuid4()),
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


    workflow_count = int(os.environ.get("WORKFLOW_COUNT"))

    # Example list
    a = [0]*workflow_count

    # Chunk size
    n = 500

    # Using list comprehension to create chunks
    chunks = [a[i:i + n] for i in range(0, len(a), n)]
    for chunk in chunks:
        tasks = [create_workflow(client, x) for x in chunk]
        await asyncio.gather(*tasks)
        ## print number or elements processed in the chunk
        time.sleep(0.3)


    await asyncio.sleep(20)
if __name__ == "__main__":
    asyncio.run(main())
