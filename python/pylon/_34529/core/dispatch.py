"""Client-side dispatch activities.

A workflow cannot call UpdateWorkflow on another workflow directly, so each operation
the parent invokes on a sandbox handle is an activity that uses the Temporal client to
`execute_update` against the sandbox workflow and waits for completion.

Idempotency comes from a deterministic `update_id` generated in the parent workflow
(`workflow.uuid4()`): on replay the server deduplicates the update. Failures are made
non-retryable, preserving the handler's error type (e.g. SandboxWorkerLost) so the
parent can branch on it.
"""

from __future__ import annotations

from dataclasses import dataclass

from temporalio import activity
from temporalio.client import Client
from temporalio.exceptions import ApplicationError

from .sandbox_workflow import (
    ExecuteActivityInput,
    ExecuteActivityResult,
    SandboxInitInput,
    SandboxWorkflow,
)

from .compute import ProviderDetails


@dataclass
class SendInitInput:
    sandbox_id: str
    update_id: str
    provider: ProviderDetails
    session_ref: str = ""


@dataclass
class SendExecuteActivityInput:
    sandbox_id: str
    update_id: str
    sleepTimeSeconds: int


def _wrap_update_error(e: Exception) -> ApplicationError:
    # A failed update surfaces as an error whose `cause` is the handler's
    # ApplicationError; preserve its type so the parent can branch on it.
    cause = getattr(e, "cause", None)
    if isinstance(cause, ApplicationError):
        return ApplicationError(cause.message, type=cause.type, non_retryable=True)
    if isinstance(e, ApplicationError):
        return ApplicationError(e.message, type=e.type, non_retryable=True)
    return ApplicationError(str(e), type="UpdateWorkflowFailure", non_retryable=True)


class DispatchActivities:
    """Holds the Temporal client used to forward updates. Register the bound methods
    on the worker (see registration.register)."""

    def __init__(self, client: Client):
        self._client = client

    @activity.defn(name="send-sandbox-init")
    async def send_init(self, inp: SendInitInput) -> None:
        handle = self._client.get_workflow_handle(inp.sandbox_id)
        try:
            await handle.execute_update(
                SandboxWorkflow.init,
                SandboxInitInput(inp.provider, inp.session_ref),
                id=inp.update_id,
            )
        except Exception as e:
            raise _wrap_update_error(e)

    @activity.defn(name="send-sandbox-run-activity")
    async def send_execute_activity(
        self, inp: SendExecuteActivityInput
    ) -> ExecuteActivityResult:
        handle = self._client.get_workflow_handle(inp.sandbox_id)
        try:
            return await handle.execute_update(
                SandboxWorkflow.execute_activity,
                ExecuteActivityInput(inp.sleepTimeSeconds),
                id=inp.update_id,
            )
        except Exception as e:
            raise _wrap_update_error(e)
