import argparse
import asyncio
from typing import Optional

from temporalio.client import Client
from temporalio.service import TLSConfig

from worker import GreetingWorkflow, dataConverter


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

    # Run workflow
    result = await client.execute_workflow(
        GreetingWorkflow.run,
        "Temporal",
        id=f"encryption-workflow-id",
        task_queue="encryption-task-queue",
    )
    print(f"Workflow result: {result}")


    try:
        await client.get_workflow_handle("encryption-workflow-id").query("non-existing-query")
    except BaseException as exc:
        print(exc)


if __name__ == "__main__":
    asyncio.run(main())
