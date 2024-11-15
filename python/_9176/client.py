import asyncio
import dataclasses

import temporalio.converter
from temporalio import workflow
from temporalio.api.cloud.cloudservice.v1 import GetNamespaceRequest
from temporalio.client import CloudOperationsClient
from temporalio.converter import DefaultFailureConverter
from temporalio.worker import Worker


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
    api_key = "..."
    namespace = "antonio.a2dd6"

    # Connect client
    client = await CloudOperationsClient.connect(
        api_key=api_key,
        version="2024-10-01-00",
        # target_host="antonio.a2dd6.tmprl.cloud:7233"
    )

    result = await client.cloud_service.get_namespace(
        GetNamespaceRequest(namespace=namespace)
    )

    print("" + result.namespace.namespace)

    result = await client.cloud_service.get_users(
        temporalio.api.cloud.cloudservice.v1.GetUsersRequest(namespace=namespace)
    )

    for user in result.users:
        print("user-----")
        print(user.id)
        print(user.spec)
        print(user.created_time)




if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    try:
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        interrupt_event.set()
        loop.run_until_complete(loop.shutdown_asyncgens())
