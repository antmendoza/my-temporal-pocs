import time

from temporalio import activity
from temporalio.exceptions import ApplicationError

print("Activity module loaded")

@activity.defn
def compose_greeting(input_data: str) -> str:

    start_time = time.time()

    for i in range(5):
        # Simulate some work
       time.sleep(0.1)

    end_time = time.time()

    print("Activity function called with input:", input_data)
    raise ApplicationError("Activity error from activity")

   # return f" Activity duration: {end_time - start_time} seconds!"
