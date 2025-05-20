from datetime import timedelta
from multiprocessing import RawValue
from typing import Sequence

from temporalio import workflow



with workflow.unsafe.imports_passed_through():
    from activity import compose_greeting


# Basic workflow that logs and invokes an activity

@workflow.defn
class GreetingWorkflow:



    def __init__(self):
        self.blocked = True

    @workflow.run
    async def run(self, request: str) -> str:

        workflow.logger.info(f"Running workflow with parameter {request}")




        for i in range(1):
            await workflow.execute_activity(
                compose_greeting,
                request+str(i),
                start_to_close_timeout=timedelta(seconds=20),
            )

        return "seconds_"
