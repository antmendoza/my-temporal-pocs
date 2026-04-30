import asyncio


from temporalio import workflow
from temporalio.client import Client
from temporalio.contrib.opentelemetry import TracingInterceptor
from temporalio.worker import Worker

from open_telemetry import interceptor
from open_telemetry.init_runtime import init_runtime_with_telemetry
from open_telemetry.workflow import GreetingWorkflow

from open_telemetry.activity import compose_greeting

import logging


interrupt_event = asyncio.Event()


async def main():
    runtime = init_runtime_with_telemetry()

    logging.basicConfig(level=logging.INFO)


    # Connect client
    client = await Client.connect(
        "localhost:7233",
        # Use OpenTelemetry interceptor
        interceptors=[
            TracingInterceptor(),
            interceptor.ContextPropagationInterceptor(),
        ],
        runtime=runtime,
    )

    # Run a worker for the workflow
    async with Worker(
        client,
        task_queue="open_telemetry-task-queue",
        workflows=[GreetingWorkflow],
        activities=[compose_greeting],
    ):
        # Wait until interrupted
        print("Worker started, ctrl+c to exit")
        await interrupt_event.wait()
        print("Shutting down")


if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    try:
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        interrupt_event.set()
        loop.run_until_complete(loop.shutdown_asyncgens())
