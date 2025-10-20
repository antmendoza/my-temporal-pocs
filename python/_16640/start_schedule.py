import asyncio
from datetime import timedelta
from random import random

from temporalio.client import (
    Client,
    Schedule,
    ScheduleActionStartWorkflow,
    ScheduleIntervalSpec,
    ScheduleSpec,
    ScheduleState,
)
from your_workflows import YourSchedulesWorkflow


async def main():
    client = await Client.connect("localhost:7233")

    for _i in range(500):


        i_ = _i + 1
        await client.create_schedule(
            "workflow-schedule-id" + str(_i),
            Schedule(
                action=ScheduleActionStartWorkflow(
                    YourSchedulesWorkflow.run,
                    "my schedule arg" + str(i_),
                    id="schedules-workflow-id" + str(i_),
                    task_queue="schedules-task-queue",
                ),
                spec=ScheduleSpec(
                    intervals=[ScheduleIntervalSpec(every=timedelta(minutes=i_))]
                ),
                state=ScheduleState(note="Here's a note on my Schedule."),
            ),
        )


if __name__ == "__main__":
    asyncio.run(main())
