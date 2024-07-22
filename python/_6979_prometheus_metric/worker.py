import asyncio
import logging
from datetime import timedelta
from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.worker import Worker
from temporalio.runtime import Runtime, TelemetryConfig, PrometheusConfig
from temporalio.api.workflowservice.v1 import DescribeTaskQueueRequest
from temporalio.api.taskqueue.v1 import TaskQueue

logging.basicConfig(level=logging.INFO)

# Define a simple workflow
@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        return await workflow.execute_activity(
            compose_greeting,
            name,
            start_to_close_timeout=timedelta(seconds=10),
        )

# Define a simple activity
@activity.defn
async def compose_greeting(name: str) -> str:
    return f"Hello, {name}!"

def start_prometheus_server():
    from prometheus_client import start_http_server
    start_http_server(9000)

async def describe_tq(client: Client, task_queue: str):
    async def describe_task_queue():
        request = DescribeTaskQueueRequest(namespace="default", task_queue=TaskQueue(name=task_queue))

        response = await client.workflow_service.describe_task_queue(request)
        pollers_count = len(response.pollers)
        logging.info(f"DescribeTaskQueue response for {task_queue}: {pollers_count} pollers")

    # pulling info in every 30 sec
    while True:
        await describe_task_queue()
        await asyncio.sleep(30)

async def main():
    # Configure telemetry for Prometheus
    runtime = Runtime(
        telemetry=TelemetryConfig(
            metrics=PrometheusConfig(bind_address="127.0.0.1:9000")
        )
    )

    client = await Client.connect("localhost:7233", runtime=runtime)

    # Start Prometheus metrics server
    start_prometheus_server()

    worker = Worker(
        client,
        task_queue="hello-task-queue",
        workflows=[GreetingWorkflow],
        activities=[compose_greeting],
        max_concurrent_activity_task_polls=5,  
        max_concurrent_workflow_task_polls=5, 
    )

    # Run the worker in the background
    worker_task = asyncio.create_task(worker.run())

    desc_tq_task = asyncio.create_task(describe_tq(client, "hello-task-queue"))

    await asyncio.gather(worker_task, desc_tq_task)

if __name__ == "__main__":
    asyncio.run(main())

