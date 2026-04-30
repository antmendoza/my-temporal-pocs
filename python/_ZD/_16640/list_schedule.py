import asyncio
from datetime import timedelta

from temporalio.client import Client, ScheduleUpdateInput, ScheduleUpdate


async def main() -> None:
    client = await Client.connect("localhost:7233")

    # List all schedules
    async for schedule in await client.list_schedules():
        print(f"List Schedule Info: {schedule.info}.")

        handle = client.get_schedule_handle(
            schedule.id,
        )

        async def update_schedule_simple(input: ScheduleUpdateInput) -> ScheduleUpdate:
            schedule_update = input.description.schedule
            schedule_update.policy.catchup_window = timedelta(hours=1)

            print(f"schedule_update: {schedule_update}.")

            return ScheduleUpdate(schedule=schedule_update)

        await handle.update(update_schedule_simple)

        # Just to slow down the loop to ensure we don't hit RPS limits
        await asyncio.sleep(0.3)


if __name__ == "__main__":
    asyncio.run(main())
