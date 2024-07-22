import asyncio
import logging
from temporalio.client import Client

logging.basicConfig(level=logging.INFO)

async def main():
    client = await Client.connect("localhost:7233")
    
    # Start a workflow execution
    result = await client.start_workflow(
        workflow="GreetingWorkflow",
        task_queue="hello-task-queue",
        id="hello-workflow-id",
        args=["World"],
    )
    
    print(f"Workflow result: {result}")

if __name__ == "__main__":
    asyncio.run(main())

