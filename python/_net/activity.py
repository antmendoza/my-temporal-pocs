import time

from temporalio import activity

from test_types import ComposeGreetingInput


# Basic activity that logs and does string concatenation
@activity.defn
def compose_greeting(input_data: ComposeGreetingInput) -> str:

    start_time = time.time()

    time.sleep(0.5)

    end_time = time.time()

    print(f"Activity duration: {end_time - start_time} seconds")
    return f"{input_data.greeting}, {input_data.name}!"
