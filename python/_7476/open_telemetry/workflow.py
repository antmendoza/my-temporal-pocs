from datetime import timedelta

from temporalio import workflow


with workflow.unsafe.imports_passed_through():
    from open_telemetry.activity import compose_greeting
    from opentelemetry import trace
    from shared import user_id


@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        workflow.logger.info(f"Workflow called by user {user_id.get()}")
        user_id.set("random")


        span = trace.get_current_span()
        span.set_attribute("doctor", 47)


        return await workflow.execute_activity(
            compose_greeting,
            name,
            start_to_close_timeout=timedelta(seconds=10),
        )
