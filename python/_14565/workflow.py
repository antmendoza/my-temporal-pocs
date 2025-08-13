import logging
from dataclasses import dataclass
from datetime import timedelta

from temporalio import workflow
from temporalio.common import RetryPolicy
from temporalio.workflow import continue_as_new

with workflow.unsafe.imports_passed_through():
    from activity import activity_OK


@dataclass
class Input:
    immediate_runs: int = 0
    current_run: int = 0


@workflow.defn
class GreetingWorkflow:

    @workflow.init
    def __init__(self, request: Input):
        if request is None:
            request = Input(
                immediate_runs=0,
                current_run=0)
        self.this_request = request

    @workflow.signal
    def add_immediate_run(self) -> None:
        self.this_request.immediate_runs += 1

    @workflow.run
    async def run(self, request: Input) -> str:

        while self.this_request.immediate_runs > 0:

            self.this_request.immediate_runs -= 1

            await workflow.execute_child_workflow(
                ChildGreetingWorkflow.run,
                self.this_request.immediate_runs,
                id=f"child-workflow-{self.this_request.current_run}",
            )
            # Note: Consider decrementing self.immediate_runs if repeated execution is intended.
            break

        await workflow.execute_local_activity(
            activity_OK,
            f"immediate_runs={self.this_request.current_run} activity_OK",
            start_to_close_timeout=timedelta(seconds=2),
            retry_policy=RetryPolicy(
                maximum_attempts=2,
            ),
        )


        if self.this_request.current_run < 10:
            new_input = Input(
                immediate_runs = self.this_request.immediate_runs,
                current_run= self.this_request.current_run + 1
            )
            continue_as_new(new_input)

        return "seconds_"


@workflow.defn
class ChildGreetingWorkflow:
    @workflow.run
    async def run(self, request: int) -> str:
        await workflow.sleep(1)
        return "ChildGreetingWorkflow completed with input: " + str(request)
