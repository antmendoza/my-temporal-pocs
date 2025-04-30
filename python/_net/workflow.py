import asyncio
import time
from datetime import timedelta

from temporalio import workflow

from fake import fake

with workflow.unsafe.imports_passed_through():
    from activity import compose_greeting
    from test_types import ComposeGreetingInput, ComposeGreetingRequest


# Basic workflow that logs and invokes an activity

@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, request: ComposeGreetingRequest) -> str:

        workflow.logger.info(f"Running workflow with parameter {request.name}")

        for i in range(10):

            fake()
            await workflow.execute_activity(
                compose_greeting,
                ComposeGreetingInput(greeting="Hello", name=request.name),
                start_to_close_timeout=timedelta(seconds=2),
            )

        return "seconds_"
