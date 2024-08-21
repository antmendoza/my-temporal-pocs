import asyncio

from temporalio.client import Client
from temporalio.contrib.opentelemetry import TracingInterceptor

from open_telemetry.init_runtime import init_runtime_with_telemetry
from open_telemetry.workflow import GreetingWorkflow

import logging

import interceptor
import shared

async def main():

    logging.basicConfig(level=logging.INFO)

    runtime = init_runtime_with_telemetry()

    # Set the user ID
    shared.user_id.set("some-user")


    # Connect client
    client = await Client.connect(
        "localhost:7233",
        # Use OpenTelemetry interceptor and Use our interceptor
        interceptors=[
            TracingInterceptor(),
            interceptor.ContextPropagationInterceptor(),
        ],
        runtime=runtime,
    )

    # Run workflow
    result = await client.execute_workflow(
        GreetingWorkflow.run,
        "Temporal",
        id=f"open_telemetry-workflow-id",
        task_queue="open_telemetry-task-queue",
    )
    print(f"Workflow result: {result}")


if __name__ == "__main__":
    asyncio.run(main())
