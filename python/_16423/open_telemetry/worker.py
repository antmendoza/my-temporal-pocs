import asyncio
from datetime import timedelta

from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.contrib.opentelemetry import TracingInterceptor
from temporalio.contrib.opentelemetry import workflow as otel_workflow
from temporalio.runtime import OpenTelemetryConfig, Runtime, TelemetryConfig
from temporalio.worker import Worker
from temporalio.workflow import unsafe


with workflow.unsafe.imports_passed_through():
    from opentelemetry.sdk.resources import SERVICE_NAME, Resource  # type: ignore
    from opentelemetry import trace
    from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
    from opentelemetry.sdk.resources import SERVICE_NAME, Resource  # type: ignore
    from opentelemetry.sdk.trace import TracerProvider
    from opentelemetry.sdk.trace.export import BatchSpanProcessor

_custom_provider = None
_custom_tracer = None


def _get_custom_tracer():
    global _custom_provider, _custom_tracer
    if _custom_provider is None:
        _custom_provider = TracerProvider(resource=Resource.create({SERVICE_NAME: "wf-e2e-duration"}))
        _exporter = OTLPSpanExporter(endpoint="http://localhost:4317", insecure=True)
        _custom_provider.add_span_processor(BatchSpanProcessor(_exporter))
        _custom_tracer = _custom_provider.get_tracer("wf-e2e-duration")
    return _custom_tracer, _custom_provider

@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        await asyncio.sleep(1)  # Simulate some processing time

        await workflow.execute_activity(
            compose_greeting,
            name,
            start_to_close_timeout=timedelta(seconds=10),
        )

        await asyncio.sleep(10)  # Simulate some more processing time

        duration = workflow.now() - workflow.info().start_time

        with unsafe.sandbox_unrestricted():

            # check if the workflow is replaying
            if not unsafe.is_replaying():
                # create custom span with workflow start time and duration attribute
                custom_tracer, custom_provider = _get_custom_tracer()
                with custom_tracer.start_span(
                    "wf-e2e-duration-span",
                    attributes={"duration": str(duration)},
                    start_time=int(workflow.info().start_time.timestamp() * 1e9)
                ):
                    pass
                custom_provider.force_flush()


        # emit workflow span with workflow duration
        duration = workflow.now() - workflow.info().start_time
        otel_workflow.completed_span(
            "e2e-workflow-duration",
            attributes={"duration": str(duration)},
        )


        return "done"


@activity.defn
async def compose_greeting(name: str) -> str:
    return f"Hello, {name}!"


interrupt_event = asyncio.Event()


def init_runtime_with_telemetry() -> Runtime:
    # Setup global tracer for workflow traces
    provider = TracerProvider(resource=Resource.create({SERVICE_NAME: "my-service"}))
    exporter = OTLPSpanExporter(endpoint="http://localhost:4317", insecure=True)
    provider.add_span_processor(BatchSpanProcessor(exporter))
    trace.set_tracer_provider(provider)

    # Setup SDK metrics to OTel endpointx
    return Runtime(
        telemetry=TelemetryConfig(
            metrics=OpenTelemetryConfig(url="http://localhost:4317")
        )
    )


async def main():
    runtime = init_runtime_with_telemetry()

    # Connect client
    client = await Client.connect(
        "localhost:7233",
        # Use OpenTelemetry interceptor
        interceptors=[TracingInterceptor(
            # always_create_workflow_spans=True
        )],
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
