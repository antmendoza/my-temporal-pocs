import temporalio.converter

import argparse
import asyncio
import dataclasses
from typing import Optional

import temporalio.converter
import temporalio.converter
from temporalio import workflow
from temporalio.client import Client
from temporalio.service import TLSConfig
from temporalio.worker import Worker

from failure_converter import FailureConverterWithDecodedAttributes
from codec import EncryptionCodec


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
    # Load certs from CLI args
    parser = argparse.ArgumentParser(description="Use mTLS with server")
    parser.add_argument(
        "--target-host", help="Host:port for the server", default="localhost:7233"
    )
    parser.add_argument(
        "--namespace", help="Namespace for the server", default="default"
    )
    parser.add_argument(
        "--server-root-ca-cert", help="Optional path to root server CA cert"
    )
    parser.add_argument(
        "--client-cert", help="Required path to client cert", required=True
    )
    parser.add_argument(
        "--client-key", help="Required path to client key", required=True
    )
    args = parser.parse_args()
    server_root_ca_cert: Optional[bytes] = None
    if args.server_root_ca_cert:
        with open(args.server_root_ca_cert, "rb") as f:
            server_root_ca_cert = f.read()
    with open(args.client_cert, "rb") as f:
        client_cert = f.read()
    with open(args.client_key, "rb") as f:
        client_key = f.read()

    # Connect client
    client = await Client.connect(

        # Use the default converter, but change the codec
        args.target_host,
        namespace=args.namespace,
        tls=TLSConfig(
            server_root_ca_cert=server_root_ca_cert,
            client_cert=client_cert,
            client_private_key=client_key,
        ),
        data_converter=await dataConverter(),
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


async def dataConverter():
    return dataclasses.replace(
        temporalio.converter.default(),
        payload_codec=EncryptionCodec(),
        failure_converter_class=FailureConverterWithDecodedAttributes,
    )



if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    try:
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        interrupt_event.set()
        loop.run_until_complete(loop.shutdown_asyncgens())
