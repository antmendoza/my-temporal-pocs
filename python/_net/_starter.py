import asyncio
import concurrent

from temporalio.client import Client
from temporalio.common import WorkflowIDReusePolicy
from temporalio.converter import (
    DataConverter,
)
from temporalio.worker import Worker

from activity import compose_greeting
from codec import EncryptionCodec
from converter import PydanticPayloadConverter
from test_types import ComposeGreetingRequest
from workflow import GreetingWorkflow


async def main():
    # Uncomment the lines below to see logging output
    # import logging

    #    os.environ["RUST_LOG"] = "temporal_sdk_core=DEBUG"

    #    logging.basicConfig(level=logging.DEBUG)

    # runtime = init_runtime_with_prometheus(8086)

    # Start client
    client = await Client.connect(
        "localhost:7233",
        # runtime=runtime,
        data_converter=DataConverter(
            payload_converter_class=PydanticPayloadConverter,
            payload_codec=EncryptionCodec()
        )
    )



#    for i in range(20):
#        asyncio.create_task(start_workflow(client, i))
#        await asyncio.sleep(0.5)

    tasks = [asyncio.create_task(start_workflow(client, i))
         for i in range(20)]

    await  asyncio.gather(*tasks)


async def start_workflow(client, i):
    # While the worker is running, use the client to run the workflow and
    # print out its result. Note, in many production setups, the client
    # would be in a completely separate process from the worker.
    workflowId = "hello-activity-workflow-id-" + str(i)
    result = await client.execute_workflow(
        GreetingWorkflow.run,
        ComposeGreetingRequest(id=str(i), name="World"),
        id=workflowId,
        task_queue="hello-activity-task-queue",
        # task_timeout=timedelta(seconds=120)
        id_conflict_policy=WorkflowIDReusePolicy.ALLOW_DUPLICATE
    )
    print(f"completed workflow id : {workflowId}")


if __name__ == "__main__":
    asyncio.run(main())
