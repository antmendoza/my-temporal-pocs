import datetime
import logging
import math
import uuid

from temporalio.testing import WorkflowEnvironment
from temporalio.worker import Worker

from workflow import GreetingWorkflow, Input


async def test_updatable_timer_workflow():
    logging.basicConfig(level=logging.DEBUG)

    task_queue_name = str(uuid.uuid4())
    async with await WorkflowEnvironment.start_time_skipping() as env:
        async with Worker(env.client, task_queue=task_queue_name, workflows=[GreetingWorkflow]):

            handle = await env.client.start_workflow(
                GreetingWorkflow.run,
                Input(immediate_runs=5, current_run=0),
                id="workflowId",
                task_queue=task_queue_name,
            )

            await handle.signal("add_immediate_run")

            await handle.result()