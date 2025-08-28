import asyncio

from temporalio.client import Client
from temporalio.common import WorkflowIDReusePolicy
from datetime import timedelta

from open_telemetry_with_collector.worker import GreetingWorkflow

async def main():

    client = await Client.connect("localhost:7233")

    await client.execute_workflow(
        ...
        task_timeout=timedelta(seconds=120)
    )
    print(f"Result: {result}")


if __name__ == "__main__":
    asyncio.run(main())
