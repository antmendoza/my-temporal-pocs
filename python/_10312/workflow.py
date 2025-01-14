from datetime import timedelta

from temporalio import workflow

with workflow.unsafe.imports_passed_through():
    from activity import compose_greeting
    from test_types import ComposeGreetingInput


# Basic workflow that logs and invokes an activity
@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        workflow.logger.info(f"Running workflow with parameter {name}")
        seconds_ = await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=10),
        )

        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=10),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=10),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=10),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=10),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=10),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=10),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=10),
        )
        await workflow.execute_activity(
            compose_greeting,
            ComposeGreetingInput(greeting="Hello", name=name),
            start_to_close_timeout=timedelta(seconds=10),
        )

        return seconds_
