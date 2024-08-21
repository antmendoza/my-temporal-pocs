from temporalio import activity

import shared


@activity.defn
async def say_hello_activity(name: str) -> str:
    activity.logger.info(f"Activity called by user {shared.user_id.get()}")
    return f"Hello, {name}"
