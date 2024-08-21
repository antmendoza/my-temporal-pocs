from datetime import timedelta

from temporalio import workflow

from open_telemetry.activity import compose_greeting

with workflow.unsafe.imports_passed_through():
    from open_telemetry.activity import compose_greeting

@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        return await workflow.execute_activity(
            compose_greeting,
            name,
            start_to_close_timeout=timedelta(seconds=10),
        )
