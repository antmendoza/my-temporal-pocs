import asyncio
import concurrent

from temporalio.client import Client
from temporalio.runtime import Runtime, TelemetryConfig, PrometheusConfig
from temporalio.worker import Worker

from activity import compose_greeting
from interceptor import ActivityRetryInterceptor
from workflow import GreetingWorkflow




async def main():
    # Uncomment the lines below to see logging output
    # import logging

    #    os.environ["RUST_LOG"] = "temporal_sdk_core=DEBUG"

    #    logging.basicConfig(level=logging.DEBUG)


    # Start client
    client = await Client.connect(
        "localhost:7233",
    )

    with concurrent.futures.ThreadPoolExecutor(max_workers=400) as activity_executor:
        worker = Worker(
            client,
            task_queue="hello-activity-task-queue",
            workflows=[GreetingWorkflow],
            activities=[compose_greeting],
            max_cached_workflows=4,
            # max_concurrent_workflow_tasks=10,
            max_concurrent_workflow_task_polls=50,
            nonsticky_to_sticky_poll_ratio=0.5,
            max_concurrent_activity_task_polls=20,
            max_concurrent_activities=400,
            activity_executor=activity_executor,
            interceptors=[ActivityRetryInterceptor()],
        )

        await worker.run()


if __name__ == "__main__":
    asyncio.run(main())
