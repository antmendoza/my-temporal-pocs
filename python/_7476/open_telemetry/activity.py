from opentelemetry import trace

from temporalio import activity


@activity.defn
async def compose_greeting(name: str) -> str:
    span = trace.get_current_span()
    span.set_attribute("total_bytes_processed", 47)
    return f"Hello, {name}!"
