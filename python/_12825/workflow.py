import asyncio
from datetime import timedelta

from temporalio import workflow


with workflow.unsafe.imports_passed_through():
    from activity import compose_greeting

from datetime import timedelta
from typing import Any
from temporalio import workflow


with workflow.unsafe.imports_passed_through():
    from pydantic import BaseModel, Field, field_validator, model_validator  # noqa: F401
    from temporalio.contrib.pydantic import pydantic_data_converter  # noqa: F401

    import sniffio
    import logging
    logger = logging.getLogger(__name__)



# Basic workflow that logs and invokes an activity

@workflow.defn
class GreetingWorkflow:

    workflow.logger
    logger.info("Workflow module loaded")

    f = open("_worker.py", "rb")

    def __init__(self):
        self.blocked = True

    @workflow.run
    async def run(self, request: str) -> str:

        workflow.logger.info(f"Running workflow with parameter {request}")

        sniffio.thread_local.names["workflow"] = "workflow"



        for i in range(1):
            await workflow.execute_activity(
                compose_greeting,
                request+str(i),
                start_to_close_timeout=timedelta(seconds=20),
            )

            await asyncio.sleep(1)


        return "seconds_"
