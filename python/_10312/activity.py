import time

from temporalio import activity

from test_types import ComposeGreetingInput


# Basic activity that logs and does string concatenation
@activity.defn
def compose_greeting(input_data: ComposeGreetingInput) -> str:
    time.sleep(5)
    activity.logger.info(f"Running activity with parameter {input_data}")
    return f"{input_data.greeting}, {input_data.name}!"
