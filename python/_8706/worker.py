import asyncio
import concurrent
import time

import inspect
from dataclasses import dataclass
from datetime import timedelta
from functools import wraps
from threading import Timer
from typing import cast

from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.worker import Worker

from custom_decorator_from_python_samples.activity_utils import F


# This helps Temporal detect worker crashes faster. This should only be used for sync activities.
def sync_activity_heartbeater(fn: F) -> F:
    # We want to ensure that the type hints from the original callable are
    # available via our wrapper, so we use the functools wraps decorator
    # mypy was complaining about F not being callable but its bound to a callable type. Ignoring operator check
    is_async = inspect.iscoroutinefunction(fn) or inspect.iscoroutinefunction(fn.__call__)  # type: ignore[operator]
    if is_async:
        raise ValueError("sync_activity_heartbeater should only be used for sync activities")

    @wraps(fn)
    def wrapper(*args, **kwargs):
        heartbeat_timeout = activity.info().heartbeat_timeout
        heartbeat_timer = None
        if heartbeat_timeout:
            # Heartbeat twice as often as the timeout
            heartbeat = activity._Context.current().heartbeat
            heartbeat_timer = RepeatingTimer(
                heartbeat_timeout.total_seconds() / 2, _heartbeat_every, args=[heartbeat]
            )
            heartbeat_timer.start()

        try:
            return fn(*args, **kwargs)
        finally:
            if heartbeat_timer:
                heartbeat_timer.cancel()

    return cast(F, wrapper)


def _heartbeat_every(heartbeat_fn) -> None:
    # Heartbeat every so often while not cancelled
    heartbeat_fn()


# While we could use multiple parameters in the activity, Temporal strongly
# encourages using a single dataclass instead which can have fields added to it
# in a backwards-compatible way.
@dataclass
class ComposeGreetingInput:
    greeting: str
    name: str


# Basic activity that logs and does string concatenation
@activity.defn
@sync_activity_heartbeater
def compose_greeting(input: ComposeGreetingInput) -> str:
    activity.logger.info("Running activity with parameter %s" % input)

    for i in iter(range(10)):
        print("activity running ...%s" % i)
        time.sleep(1)

    return f"{input.greeting}, {input.name}!"


# Basic workflow that logs and invokes an activity
@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        workflow.logger.info("Running workflow with parameter %s" % name)

        return await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput("Hello", name),
            heartbeat_timeout=timedelta(seconds=2),
            start_to_close_timeout=timedelta(seconds=20),
            # Tell the workflow to wait for the post-cancel result
            cancellation_type=workflow.ActivityCancellationType.WAIT_CANCELLATION_COMPLETED,

        )


async def main():
    # Uncomment the lines below to see logging output
    # import logging
    # logging.basicConfig(level=logging.INFO)

    # Start client
    client = await Client.connect("localhost:7233")


    with concurrent.futures.ThreadPoolExecutor(max_workers=100) as activity_executor:
        worker = Worker(
            client,
            task_queue="hello-activity-task-queue",
            workflows=[GreetingWorkflow],
            activities=[compose_greeting],
            activity_executor=activity_executor,
        )
        await worker.run()

class RepeatingTimer(Timer):
    def run(self):
        while not self.finished.is_set():
            print("heartbeting ...")
            self.function(*self.args, **self.kwargs)
            self.finished.wait(self.interval)





if __name__ == "__main__":
    asyncio.run(main())
