import asyncio
from datetime import timedelta

from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.contrib.opentelemetry import TracingInterceptor
from temporalio.runtime import OpenTelemetryConfig, Runtime, TelemetryConfig, OpenTelemetryMetricTemporality, \
    PrometheusConfig
from temporalio.worker import Worker

import os


@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        return await workflow.execute_activity(
            compose_greeting,
            name,
            start_to_close_timeout=timedelta(seconds=10),
        )


@activity.defn
async def compose_greeting(name: str) -> str:
    return f"Hello, {name}!"


interrupt_event = asyncio.Event()


def init_runtime_with_telemetry() -> Runtime:


    ## if env variable prometheus-port is not null setup prometheus
    prometheus_port = os.environ.get("PROMETHEUS_PORT")
    if prometheus_port is not None:
        return  Runtime(
            telemetry=TelemetryConfig(metrics=PrometheusConfig("127.0.0.1:" + prometheus_port))
        )
    else:
        # Setup SDK metrics to OTel endpoint
        return Runtime(
            telemetry=TelemetryConfig(
                metrics=OpenTelemetryConfig(
                    url="http://localhost:4317",
                    metric_periodicity=timedelta(seconds=1),
                    metric_temporality=OpenTelemetryMetricTemporality.DELTA,
                )
            )
        )



async def main():
    runtime = init_runtime_with_telemetry()

    # Connect client
    client = await Client.connect(
        "localhost:7233",
        # Use OpenTelemetry interceptor
        interceptors=[TracingInterceptor()],
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
