import asyncio
import dataclasses

import temporalio.converter
from temporalio import workflow
from temporalio.client import Client
from temporalio.converter import DefaultFailureConverter
from temporalio.worker import Worker

from codec import EncryptionCodec


class FailureConverterWithDecodedAttributes(DefaultFailureConverter):
    def __init__(self) -> None:
        super().__init__(encode_common_attributes=True)

@workflow.defn(name="Workflow")
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        return f"Hello, {name}"

    @workflow.query
    def greeting(self) -> str:
        return "test"



interrupt_event = asyncio.Event()


async def main():
    # Connect client
    client = await Client.connect(
        "localhost:7233",
        # Use the default converter, but change the codec
        data_converter=dataclasses.replace(
            temporalio.converter.default(),
            payload_codec=EncryptionCodec(),
            failure_converter_class=FailureConverterWithDecodedAttributes,
        ),
    )

    # Run a worker for the workflow
    async with Worker(
        client,
        task_queue="encryption-task-queue",
        workflows=[GreetingWorkflow],
    ):
        # Wait until interrupted
        print("Worker started, ctrl+c to exit")
        await interrupt_event.wait()
        print("Shutting down")


if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    try:
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        interrupt_event.set()
        loop.run_until_complete(loop.shutdown_asyncgens())
