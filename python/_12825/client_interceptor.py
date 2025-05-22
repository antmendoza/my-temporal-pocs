from typing import Any, Dict, List, Optional, Tuple, Type

from temporalio import workflow
from temporalio.worker import (
    HandleSignalInput,
    Interceptor,
    StartActivityInput,
    WorkflowInboundInterceptor,
    WorkflowInterceptorClassInput,
    WorkflowOutboundInterceptor,
)


class RetryOnSignalWorkerInterceptor(Interceptor):
    def __init__(self, max_attempts: int = 3):
        self.max_attempts = max_attempts

    def workflow_interceptor_class(
            self, input: WorkflowInterceptorClassInput
    ) -> Optional[Type[WorkflowInboundInterceptor]]:
        max_attempts = self.max_attempts

        # Create a fresh subclass so we can bind max_attempts from this instance
        class _BoundInbound(RetryOnSignalWorkflowInboundInterceptor):
            def init(self, outbound: WorkflowOutboundInterceptor) -> None:
                # per-workflow state
                self.max_attempts = max_attempts
                self._failure_counts: Dict[str, int] = {}
                self._instructions: Dict[str, str] = {}
                # wire in our custom outbound interceptor
                super().init(RetryOnSignalWorkflowOutboundInterceptor(outbound, self))

        return _BoundInbound


class RetryOnSignalWorkflowInboundInterceptor(WorkflowInboundInterceptor):
    async def handle_signal(self, input: HandleSignalInput) -> None:
        # Expect (activity_id, "retry"|"skip")
        activity_id, instruction = input.input
        if activity_id and instruction in ("retry", "skip"):
            self._instructions[activity_id] = instruction
        # still deliver the signal to your workflow code
        return await super().handle_signal(input)

class RetryOnSignalWorkflowOutboundInterceptor(WorkflowOutboundInterceptor):
    def __init__(
            self,
            next_outbound: WorkflowOutboundInterceptor,
            inbound: RetryOnSignalWorkflowInboundInterceptor,
    ):
        super().__init__(next_outbound)
        self._inbound = inbound

    def start_activity(self, input: StartActivityInput) -> workflow.ActivityHandle:
        original_handle = super().start_activity(input)

        class _HandleWrapper(workflow.ActivityHandle):
            def __init__(self_wrapper, handle):
                self_wrapper._handle = handle
                self_wrapper._activity_id = input.activity_id or str(workflow.uuid4())

            def __await__(self_wrapper):
                return self_wrapper._execute().__await__()

            async def _execute(self_wrapper):
                while True:
                    try:
                        result = await self_wrapper._handle
                        return result
                    except Exception as err:
                        raise err

            def __getattr__(self, name):
                return getattr(self._handle, name)

        return _HandleWrapper(original_handle)