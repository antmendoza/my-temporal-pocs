from temporalio import workflow
from temporalio.common import RetryPolicy
from temporalio.exceptions import ActivityError

with workflow.unsafe.imports_passed_through():
    from activity import activity_KO, activity_OK

from datetime import timedelta
from temporalio import workflow



# Basic workflow that logs and invokes an activity

@workflow.defn
class GreetingWorkflow:



    def __init__(self):
        self.blocked = True

    @workflow.run
    async def run(self, request: str) -> str:
        logger = workflow.logger
        logger.info("Workflow starting...")


        try:
            await workflow.execute_activity(
                activity_KO,
                request + " activity_KO",
                start_to_close_timeout=timedelta(seconds=20),
                retry_policy=RetryPolicy(
                    maximum_attempts=2,
                ),
            )
        except ActivityError:
            logger.error("activity_KO error ignoring-----")

        try:
            await workflow.execute_activity(
                activity_OK,
                request + " activity_OK",
                start_to_close_timeout=timedelta(seconds=20),
                retry_policy=RetryPolicy(
                    maximum_attempts=2,
                ),
            )
        except ActivityError:
            logger.error("activity_OK error ignoring-----")

        return "seconds_"
