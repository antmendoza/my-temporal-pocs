from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.common import RetryPolicy
from temporalio.worker import Worker
from contextvars import copy_context
from datetime import datetime, timedelta
from typing import Any, Type, TypeVar, Callable, Awaitable, cast
from concurrent.futures import ThreadPoolExecutor
import asyncio
import threading
import temporalio.converter
import temporalio.workflow
import time


F = TypeVar("F", bound=Callable[..., Awaitable[Any]])


def auto_heartbeat(fn: F) -> F:
    setattr(fn, "__temporal_auto_heartbeat", True)
    return fn


class Interceptor(
    temporalio.client.Interceptor, temporalio.worker.Interceptor
):
    """Interceptor that can serialize/deserialize contexts."""

    def __init__(
        self,
        executor: ThreadPoolExecutor,
        payload_converter: temporalio.converter.PayloadConverter = temporalio.converter.default().payload_converter,
    ) -> None:
        self._payload_converter = payload_converter
        self._executor = executor

    def intercept_activity(
        self, next: temporalio.worker.ActivityInboundInterceptor
    ) -> temporalio.worker.ActivityInboundInterceptor:
        return _ActivityInboundInterceptor(next, self._executor)


class _ActivityInboundInterceptor(
    temporalio.worker.ActivityInboundInterceptor
):
    def __init__(
            self,
            next: temporalio.worker.ActivityInboundInterceptor,
            executor: ThreadPoolExecutor,
    ):
        super().__init__(next)
        self._executor = executor

    async def execute_activity(
        self, input: temporalio.worker.ExecuteActivityInput
    ) -> Any:
        heartbeat_timeout = activity.info().heartbeat_timeout
        guard_heartbeat = getattr(input.fn, "__temporal_auto_heartbeat") and heartbeat_timeout
        stop_heartbeat_event = None
        heartbeat_future = None
        if guard_heartbeat:
            stop_heartbeat_event = threading.Event()
            ctx = copy_context()
            heartbeat_future = asyncio.get_event_loop().run_in_executor(self._executor, ctx.run, heartbeat_every,
                                                                        stop_heartbeat_event,
                                                                        heartbeat_timeout.total_seconds() / 2)
        try:
            return await self.next.execute_activity(input)
        finally:
            if guard_heartbeat:
                print("stopping heartbeats")  # For demonstration purposes only. Feel free to remove.
                stop_heartbeat_event.set()
                await heartbeat_future


def heartbeat_every(stop_event: threading.Event, delay: float, *details: Any) -> None:
    # Heartbeat every so often while not cancelled
    while True:
        time.sleep(delay)
        activity.heartbeat(*details)
        print(f"Heartbeat at {datetime.now()}")  # This is for demonstration purposes only. Feel free to remove.
        if stop_event.is_set():
            break
    return None


@activity.defn
@auto_heartbeat
def run_forever_activity():
    # Wait forever, catch the cancel, and return some value
    while True:
        print("Hello from run_forever_activity...")
        time.sleep(1)


@workflow.defn
class WaitForCancelWorkflow:
    @workflow.run
    async def run(self) -> str:
        # Start activity and wait on it (it will get cancelled from signal)
        self.activity = workflow.start_activity(
            run_forever_activity,
            start_to_close_timeout=timedelta(hours=20),
            # We set a heartbeat timeout so Temporal knows if the activity
            # failed/crashed. If we don't heartbeat within this time, Temporal
            # will consider the activity failed.
            heartbeat_timeout=timedelta(seconds=10),
            # Tell the activity not to retry for demonstration purposes only
            retry_policy=RetryPolicy(maximum_attempts=1),
            # Tell the workflow to wait for the post-cancel result
            cancellation_type=workflow.ActivityCancellationType.WAIT_CANCELLATION_COMPLETED,
        )
        return await self.activity

    @workflow.signal
    def cancel_activity(self) -> None:
        self.activity.cancel()


interrupt_event = asyncio.Event()


async def main():
    # Max workers needs to be at least 2x max concurrent activities executed since each activity may take up 2 threads
    # This setting will bottleneck the total concurrency of the system if set too low.
    with ThreadPoolExecutor(max_workers=10) as executor:
        client = await Client.connect(
            "localhost:7233",
            # Use an interceptor to automatically heartbeat functions decorated w/ @autoheartbeat using threads
            interceptors=[Interceptor(executor)],
        )
        # Run a worker for the workflow
        async with Worker(
            client,
            task_queue="custom_decorator-task-queue",
            activities=[run_forever_activity],
            workflows=[WaitForCancelWorkflow],
            # Synchronous activities are not allowed unless we provide some kind of
            # executor. This same thread pool could be passed to multiple workers if
            # desired.
            activity_executor=executor,
        ):
            # Wait until interrupted
            print("Activity Worker started, ctrl+c to exit")

            handle = await client.start_workflow(
                WaitForCancelWorkflow.run,
                id=f"custom_decorator-workflow-id",
                task_queue="custom_decorator-task-queue",
            )

            print("Started workflow, waiting 5 seconds before cancelling")
            await asyncio.sleep(5)
            print("cancelling workflow")
            await handle.cancel()
            result = await handle.result()
            print(f"Result: {result}")

            await interrupt_event.wait()
            print("Shutting down")

if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    try:
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        interrupt_event.set()
        loop.run_until_complete(loop.shutdown_asyncgens())