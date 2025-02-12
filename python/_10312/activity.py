import time

from temporalio import activity

from test_types import ComposeGreetingInput


# Basic activity that logs and does string concatenation
@activity.defn
def compose_greeting(input_data: ComposeGreetingInput) -> str:

    start_time = time.time()

    i =0;

    while True:
        i = i+1
        if i == 500_000_000:
            break

    end_time = time.time()

    print(f"Activity duration: {end_time - start_time} seconds")
    return f"{input_data.greeting}, {input_data.name}!"
