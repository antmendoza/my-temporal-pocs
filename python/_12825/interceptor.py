from __future__ import annotations

import asyncio
from typing import Any, Type

import temporalio.activity
import temporalio.api.common.v1
import temporalio.client
import temporalio.converter
import temporalio.worker
from blackd import handle
from temporalio import workflow
from temporalio.exceptions import ApplicationError


class ActivityRetryInterceptor(
    temporalio.worker.Interceptor
):

    def __init__(
            self,
            payload_converter: temporalio.converter.PayloadConverter = temporalio.converter.default().payload_converter,
    ) -> None:
        self._payload_converter = payload_converter

    def intercept_activity(
            self, next: temporalio.worker.ActivityInboundInterceptor
    ) -> temporalio.worker.ActivityInboundInterceptor:
        return _ActivityRetryActivityInboundInterceptor(next)

    def workflow_interceptor_class(
            self, input: temporalio.worker.WorkflowInterceptorClassInput
    ) -> Type[_ActivityRetryWorkflowInboundInterceptor]:
        return _ActivityRetryWorkflowInboundInterceptor


class _ActivityRetryActivityInboundInterceptor(
    temporalio.worker.ActivityInboundInterceptor
):
    async def execute_activity(
            self, input: temporalio.worker.ExecuteActivityInput
    ) -> Any:

        print("ActivityRetryActivityInboundInterceptor execute activity")
        return await self.next.execute_activity(input)


class _ActivityRetryWorkflowInboundInterceptor(
    temporalio.worker.WorkflowInboundInterceptor
):


    def __init__(self, next: temporalio.worker.WorkflowInboundInterceptor) -> None:
        self.outbound_interceptor = None
        self.next = next



    def init(self, outbound: temporalio.worker.WorkflowOutboundInterceptor) -> None:
        self.outbound_interceptor = _ActivityRetryWorkflowOutboundInterceptor(outbound)
        self.next.init(self.outbound_interceptor)

    async def execute_workflow(
            self, input: temporalio.worker.ExecuteWorkflowInput
    ) -> Any:

        self.outbound_interceptor.initialize_handlers()
        return await self.next.execute_workflow(input)

    async def handle_signal(self, input: temporalio.worker.HandleSignalInput) -> None:
        return await self.next.handle_signal(input)

    async def handle_query(self, input: temporalio.worker.HandleQueryInput) -> Any:
        return await self.next.handle_query(input)

    def handle_update_validator(
            self, input: temporalio.worker.HandleUpdateInput
    ) -> None:
        self.next.handle_update_validator(input)

    async def handle_update_handler(
            self, input: temporalio.worker.HandleUpdateInput
    ) -> Any:
        return await self.next.handle_update_handler(input)


class _ActivityRetryWorkflowOutboundInterceptor(
    temporalio.worker.WorkflowOutboundInterceptor
):


    def __init__(self, next: temporalio.worker.WorkflowOutboundInterceptor) -> None:
        self.next = next
        self.blocked = True


    def initialize_handlers(self):
        def unblock() -> None:
            self.next.start_activity()
            self.blocked = False
        workflow.set_signal_handler("my_signal", unblock)



    async def signal_child_workflow(
            self, input: temporalio.worker.SignalChildWorkflowInput
    ) -> None:
        return await self.next.signal_child_workflow(input)


    async def signal_external_workflow(
            self, input: temporalio.worker.SignalExternalWorkflowInput
    ) -> None:
        return await self.next.signal_external_workflow(input)


    async def async_func(
            self
    ):
        await workflow.wait_condition(lambda: not self.blocked)


    def start_activity(
            self, input: temporalio.worker.StartActivityInput
    ) -> temporalio.workflow.ActivityHandle:
        handle = super().start_activity(input)


        def on_activity_done(task: asyncio.Task):
            try:
                result = task.result()
                # Handle success
            except Exception as e:
                # Handle failure
                print("Activity error Exception-----")
                workflow.wait(lambda: not self.blocked)


        handle.add_done_callback(on_activity_done)
        return handle



    async def start_child_workflow(
            self, input: temporalio.worker.StartChildWorkflowInput
    ) -> temporalio.workflow.ChildWorkflowHandle:
        return await self.next.start_child_workflow(input)


    def start_local_activity(
            self, input: temporalio.worker.StartLocalActivityInput
    ) -> temporalio.workflow.ActivityHandle:
        return self.next.start_local_activity(input)
