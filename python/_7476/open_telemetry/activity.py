from opentelemetry import trace

from temporalio import activity

import shared


@activity.defn
async def compose_greeting(name: str) -> str:
    span = trace.get_current_span()
    span.set_attribute("from_activity", 47)


    activity.logger.info(f"Activity called by user {shared.user_id.get()}")
    activity.logger.info(f"Activity span.is_recording() {span.is_recording()}")

    return f"Hello, {name}!"
