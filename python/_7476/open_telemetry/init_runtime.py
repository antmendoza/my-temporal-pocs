

from opentelemetry import trace
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.resources import SERVICE_NAME, Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor

from temporalio.runtime import Runtime, TelemetryConfig, OpenTelemetryConfig


def init_runtime_with_telemetry() -> Runtime:
    # Setup global tracer for workflow traces
    provider = TracerProvider(resource=Resource.create({SERVICE_NAME: "my-service"}))
    exporter = OTLPSpanExporter(endpoint="http://localhost:4317", insecure=True)
    provider.add_span_processor(BatchSpanProcessor(exporter))
    trace.set_tracer_provider(provider)



    # Setup SDK metrics to OTel endpoint
    return Runtime(
        telemetry=TelemetryConfig(
            metrics=OpenTelemetryConfig(url="http://localhost:4317")
        )
    )
