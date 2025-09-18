import asyncio
import os
import random
import logging
from datetime import timedelta

from opentelemetry._logs import set_logger_provider
from opentelemetry.exporter.otlp.proto.grpc._log_exporter import OTLPLogExporter
from opentelemetry.sdk._logs import LoggerProvider, LoggingHandler
from opentelemetry.sdk._logs._internal.export import BatchLogRecordProcessor
from opentelemetry.sdk.resources import Resource
from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.contrib.opentelemetry import TracingInterceptor
from temporalio.runtime import OpenTelemetryConfig, Runtime, TelemetryConfig, PrometheusConfig, \
    OpenTelemetryMetricTemporality
from temporalio.worker import Worker


@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        seconds_ = await workflow.execute_activity(compose_greeting, name,
                                                   start_to_close_timeout=timedelta(seconds=60))

        print(f"Workflow completing")
        return seconds_


@activity.defn
async def compose_greeting(name: str) -> str:

    if name:
        raise Exception("simulated error")

    ## calculate random number smaller than 10
    random_number = random.randint(1, 10)
    await asyncio.sleep(random_number)
    return f"Hello, {name}!"


interrupt_event = asyncio.Event()


def init_runtime_with_telemetry() -> Runtime:
    ## if env variable prometheus-port is not null setup prometheus
    prometheus_port = os.environ.get("PROMETHEUS_PORT")
    if prometheus_port is not None:
        return Runtime(
            telemetry=TelemetryConfig(
                metrics=PrometheusConfig(
                    bind_address="127.0.0.1:" + prometheus_port,
                    histogram_bucket_overrides={
                        #             "temporal_activity_schedule_to_start_latency": [1, 10, 30, 60, 120, 300, 600, 1800, 3600]
                    }
                ),
                global_tags={"anything": "worker_" + prometheus_port},
            )
        )
    else:
        # Setup SDK metrics to OTel endpoint
        WORKER_ID = "worker"

        return Runtime(
            telemetry=TelemetryConfig(
                metrics=OpenTelemetryConfig(
                    url="http://localhost:4317",
                    metric_periodicity=timedelta(seconds=1),
                    metric_temporality=OpenTelemetryMetricTemporality.DELTA
                ),
                global_tags={"anything": "worker_" + WORKER_ID,
                             "env": "worker_" + WORKER_ID,
                             "env33": "worker_" + WORKER_ID
                             },

            )
        )


def init_otel_logging(collector_endpoint: str = "http://localhost:4317") -> None:
    """Configure OpenTelemetry logging to export to an OTLP gRPC collector.

    This sends Python logs to the local Datadog-enabled collector on port 4317.
    """
    # Basic resource so logs are attributed in Datadog
    resource = Resource.create({
        "service.name": os.getenv("OTEL_SERVICE_NAME", "temporal-worker"),
        "service.version": os.getenv("OTEL_SERVICE_VERSION", "1.0.0"),
        "deployment.environment": os.getenv("OTEL_ENV", "dev"),
    })

    logger_provider = LoggerProvider(resource=resource)
    exporter = OTLPLogExporter(endpoint=collector_endpoint, insecure=True)
    logger_provider.add_log_record_processor(BatchLogRecordProcessor(exporter))
    set_logger_provider(logger_provider)

    # Bridge Python logging to OTel
    handler = LoggingHandler(level=logging.INFO, logger_provider=logger_provider)
    root_logger = logging.getLogger()
    root_logger.setLevel(logging.INFO)
    root_logger.addHandler(handler)


async def main():
    runtime = init_runtime_with_telemetry()
    # Send application logs to the OTLP collector (Datadog via collector)
   # init_otel_logging(collector_endpoint=os.getenv("OTLP_LOGS_ENDPOINT", "http://localhost:4317"))

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
            #            max_concurrent_activities=100,
    ):
        # Wait until interrupted
        print("Worker started, ctrl+c to exit")
        await interrupt_event.wait()
        print("Shutting down")


if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    interrupt_event = asyncio.Event()  # Now it's bound to this loop
    try:
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        interrupt_event.set()
        loop.run_until_complete(loop.shutdown_asyncgens())
