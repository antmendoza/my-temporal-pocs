import asyncio
import dataclasses

import temporalio.converter
from temporalio.client import Client

from codec import EncryptionCodec
from worker import GreetingWorkflow


async def main():
    # Connect client
    client = await Client.connect(
        "localhost:7233",
        # Use the default converter, but change the codec
        data_converter=dataclasses.replace(
            temporalio.converter.default(), payload_codec=EncryptionCodec()
        ),
    )

    # Run workflow
    result = await client.execute_workflow(
        GreetingWorkflow.run,
        "Temporal",
        id=f"encryption-workflow-id",
        task_queue="encryption-task-queue",
    )
    print(f"Workflow result: {result}")

    try:
        query = "greeting"
        print("> querying workflow." + query + "")
        result = await client.get_workflow_handle("encryption-workflow-id").query(query)
        print("result workflow." + query + "= " +  result)

    except BaseException as exc:
        print(exc)

    try:
        query = "non-existing-query"
        print("> querying workflow." + query + "")
        await client.get_workflow_handle("encryption-workflow-id").query(query)
        print("result workflow." + query + "= " +  result)
    except BaseException as exc:
        print(exc)


if __name__ == "__main__":
    asyncio.run(main())
