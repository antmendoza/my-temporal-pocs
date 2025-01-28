import argparse
import asyncio
from dataclasses import dataclass
from datetime import timedelta
from typing import Optional

from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.common import RetryPolicy
from temporalio.service import TLSConfig
from temporalio.worker import Worker


@dataclass
class ComposeGreetingInput:
    greeting: str
    name: str

@dataclass
class MyInput:
    recomputation_date_iso: str
    name: str




@activity.defn
async def compose_greeting(input: ComposeGreetingInput) -> str:
    activity.logger.info("Running activity with parameter %s" % input)

    await asyncio.sleep(2)

    return f"{input.greeting}, {input.name}!"


# Basic workflow that logs and invokes an activity
class QueryRegistry:
    query_1 = "query_1"
    query_2 = "query_2"


@workflow.defn
class GreetingWorkflow:
    def __init__(self):
        self.some_date = None
        self.name = None

    @workflow.run
    async def run(self, input: MyInput) -> str:
#    async def run(self, input: str) -> str:

        try:
            self.some_date = (
                input.recomputation_date_iso
            )

            workflow.logger.info("Running workflow with parameter %s" % input.name)

            await workflow.execute_activity(
                compose_greeting,
                ComposeGreetingInput("Hello", input.name),
                start_to_close_timeout=timedelta(seconds=1),
                retry_policy=RetryPolicy(
                    maximum_attempts=1,
                )
            )
        except Exception as e:

            await workflow.execute_activity(
                compose_greeting,
                ComposeGreetingInput("Hello", input.name),
                start_to_close_timeout=timedelta(seconds=10),
                retry_policy=RetryPolicy(
                    maximum_attempts=1,
                )
            )
            raise e

        return ""

    @workflow.query(name=QueryRegistry.query_2)
    def query_2(self) -> str | None:
        return self.some_date

    @workflow.query(name=QueryRegistry.query_1)
    def query_1(self) -> str | None:
        return self.name


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

    # Start client with TLS configured
    client = await Client.connect(
        args.target_host,
        namespace=args.namespace,
        tls=TLSConfig(
            server_root_ca_cert=server_root_ca_cert,
            client_cert=client_cert,
            client_private_key=client_key,
        ),
    )

    # Run a worker for the workflow
    async with Worker(
            client,
            task_queue="hello-mtls-task-queue",
            workflows=[GreetingWorkflow],
            activities=[compose_greeting],
    ):

        # While the worker is running, use the client to run the workflow and
        # print out its result. Note, in many production setups, the client
        # would be in a completely separate process from the worker.
        result = await client.execute_workflow(
            GreetingWorkflow.run,
            MyInput(recomputation_date_iso="test", name="test2"),
            id="hello-mtls-workflow-id",
            task_queue="hello-mtls-task-queue",
        )
        print(f"Result: {result}")


if __name__ == "__main__":
    asyncio.run(main())
