import asyncio
import time
from datetime import timedelta

from temporalio import workflow

with workflow.unsafe.imports_passed_through():
    from activity import compose_greeting
    from test_types import ComposeGreetingInput, ComposeGreetingRequest


# Basic workflow that logs and invokes an activity


@workflow.defn(sandboxed=False)
class GreetingWorkflow:
    @workflow.run
    async def run(self, request: ComposeGreetingRequest) -> str:
        workflow.logger.info(f"Running workflow with parameter {request.name}")

        with workflow.unsafe.sandbox_unrestricted():
            time.sleep(1)


        for i in range(10):

            with workflow.unsafe.sandbox_unrestricted():
                time.sleep(1)

            await workflow.execute_activity(
                compose_greeting,
                ComposeGreetingInput(greeting="Hello", name=request.name),
                start_to_close_timeout=timedelta(seconds=2),
            )

        return "seconds_"
