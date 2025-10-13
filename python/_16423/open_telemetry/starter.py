import asyncio

from temporalio.api.enums.v1.workflow_pb2 import WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING
from temporalio.client import Client
from temporalio.contrib.opentelemetry import TracingInterceptor

from open_telemetry.worker import GreetingWorkflow, init_runtime_with_telemetry


async def main():
    runtime = init_runtime_with_telemetry()

    # Connect client
    client = await Client.connect(
        "localhost:7233",
        # Use OpenTelemetry interceptor
        interceptors=[TracingInterceptor(
        #    always_create_workflow_spans=True
        )],
        runtime=runtime,
    )

    # Run workflow
    result = await client.execute_workflow(
        GreetingWorkflow.run,
        "Temporal",
        id=f"open_telemetry-workflow-id",
        task_queue="open_telemetry-task-queue",
        id_conflict_policy=WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING
    )
    print(f"Workflow result: {result}")


if __name__ == "__main__":
    asyncio.run(main())
