from opentelemetry import trace

from temporalio import activity

import shared


@activity.defn
async def compose_greeting(name: str) -> str:
    span = trace.get_current_span()
    span.set_attribute("total_bytes_processed", 47)


    activity.logger.info(f"Activity called by user {shared.user_id.get()}")


    return f"Hello, {name}!"
