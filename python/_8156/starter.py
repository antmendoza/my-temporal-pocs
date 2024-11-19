import asyncio
import dataclasses

import temporalio.converter
from temporalio.client import Client, WorkflowQueryFailedError

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
        print("> querying workflow." + "greeting" + "")
        result = await client.get_workflow_handle("encryption-workflow-id").query("greeting")
        print("result workflow." + "greeting" + "= " + result)

    except BaseException as exc:
        print(exc)

    try:
        print("> querying workflow." + "non-existing-query" + "")
        await client.get_workflow_handle("encryption-workflow-id").query("non-existing-query")
        print("result workflow." + "non-existing-query" + "= " + result)
    except WorkflowQueryFailedError as exc:
        print("-------")
        print(exc)
        print("-------")
        print(dir(exc))


if __name__ == "__main__":
    asyncio.run(main())
