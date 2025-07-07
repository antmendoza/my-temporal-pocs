from temporalio import activity



@activity.defn
async def say_hello_activity(name: str) -> str:
    return f"Hello, {name}"
