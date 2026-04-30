import asyncio
import concurrent
import time

import inspect
from dataclasses import dataclass
from datetime import timedelta
from functools import wraps
from threading import Timer
from typing import cast, Callable, Any

from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.worker import Worker

# from custom_decorator.activity_utils import F
from worker import GreetingWorkflow


async def main():
    # Uncomment the lines below to see logging output
    # import logging
    # logging.basicConfig(level=logging.INFO)

    # Start client
    client = await Client.connect("localhost:7233")

#        disable_eager_activity_execution=True,
#        nonsticky_to_sticky_poll_ratio= 0.2 # (increase to 0.5 to ensure you have pollers for non-sticky /start child workflow)


    # While the worker is running, use the client to run the workflow and
    # print out its result. Note, in many production setups, the client
    # would be in a completely separate process from the worker.
    workflow_id = "hello-activity-workflow-id"
    result = await client.start_workflow(
        GreetingWorkflow.run,
        "World",
        id=workflow_id,
        task_queue="hello-activity-task-queue",
    )

    time.sleep(4)

    await client.get_workflow_handle(workflow_id).cancel();

    print(f"Result: {result}")


# class RepeatingTimer(Timer):
#     def run(self):
#         while not self.finished.is_set():
#             self.function(*self.args, **self.kwargs)
#             self.finished.wait(self.interval)





if __name__ == "__main__":
    asyncio.run(main())