from datetime import timedelta

from temporalio import workflow

from temporalio.contrib import opentelemetry

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

        #this does not work
        span = trace.get_current_span()
        span.set_attribute("from_workflow", 47)
        workflow.logger.info(f"span.is_recording() from workflow {span.is_recording()}")

        #this works
        #https://github.com/temporalio/sdk-python/blob/4e97841814b71440fadb33409146e7d8ee9694da/temporalio/contrib/opentelemetry.py#L183-L185
        opentelemetry.workflow.completed_span("my_span",
                                              attributes={"brand": "Ford", "model": "Mustang", "year": 1964
                                                          })





        return await workflow.execute_activity(
            compose_greeting,
            name,
            start_to_close_timeout=timedelta(seconds=10),
        )
