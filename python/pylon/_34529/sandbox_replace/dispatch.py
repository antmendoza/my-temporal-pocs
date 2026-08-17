"""Client-side dispatch activities (port of the `SendSandbox*` activities in
`sdk/sandbox_activity.go`).

A workflow cannot call UpdateWorkflow on another workflow directly, so every
operation the parent invokes on a sandbox handle is implemented as an activity
that uses the Temporal client to `execute_update` against the sandbox workflow
and waits for completion — giving synchronous request/response semantics.

Idempotency comes from a deterministic `update_id` generated in the parent
workflow (workflow.uuid4()): if the parent replays and re-runs the dispatch
activity, the server deduplicates the update so it is applied exactly once.
Failures are made non-retryable at the activity layer, preserving the domain
error type (e.g. AlreadySuspended) so callers can branch on it.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Optional

from temporalio import activity
from temporalio.client import Client
from temporalio.exceptions import ApplicationError

from .compute import CommandResult, ProviderDetails, ProviderSnapshot
from .workflow import (
    ExecuteActivityInput,
    ExecuteActivityResult,
    ExecuteCommandInput,
    SandboxInitInput,
    SandboxWorkflow,
)


@dataclass
class SendInitInput:
    sandbox_id: str
    update_id: str
    provider: ProviderDetails
    idle_timeout_seconds: float
    session_ref: str = ""
    snapshot: Optional[ProviderSnapshot] = None


@dataclass
class SendExecuteCommandInput:
    sandbox_id: str
    update_id: str
    command: str
    disable_auto_resume: bool = False

@dataclass
class SendExecuteActivityInput:
    sandbox_id: str
    update_id: str
    sleepTimeSeconds: int
    disable_auto_resume: bool = False



@dataclass
class SendSuspendInput:
    sandbox_id: str
    update_id: str


@dataclass
class SendResumeInput:
    sandbox_id: str
    update_id: str


def _wrap_update_error(e: Exception) -> ApplicationError:
    # An update rejected/failed by the handler surfaces as an error whose
    # `cause` is the handler's ApplicationError; preserve its type.
    cause = getattr(e, "cause", None)
    if isinstance(cause, ApplicationError):
        return ApplicationError(cause.message, type=cause.type, non_retryable=True)
    if isinstance(e, ApplicationError):
        return ApplicationError(e.message, type=e.type, non_retryable=True)
    return ApplicationError(str(e), type="UpdateWorkflowFailure", non_retryable=True)


class DispatchActivities:
    """Holds the Temporal client used to forward updates. Register the bound
    methods on the worker (see registration.register)."""

    def __init__(self, client: Client):
        self._client = client

    @activity.defn(name="send-sandbox-init")
    async def send_init(self, inp: SendInitInput) -> None:
        handle = self._client.get_workflow_handle(inp.sandbox_id)
        try:
            await handle.execute_update(
                SandboxWorkflow.init,
                SandboxInitInput(
                    inp.provider,
                    inp.idle_timeout_seconds,
                    inp.session_ref,
                    inp.snapshot,
                ),
                id=inp.update_id,
            )
        except Exception as e:
            raise _wrap_update_error(e)

    @activity.defn(name="send-sandbox-execute-command")
    async def send_execute_command(
        self, inp: SendExecuteCommandInput
    ) -> CommandResult:
        handle = self._client.get_workflow_handle(inp.sandbox_id)
        try:
            return await handle.execute_update(
                SandboxWorkflow.execute_command,
                ExecuteCommandInput(inp.command, inp.disable_auto_resume),
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
                ExecuteActivityInput(inp.sleepTimeSeconds, inp.disable_auto_resume),
                id=inp.update_id,
            )
        except Exception as e:
            raise _wrap_update_error(e)



    @activity.defn(name="send-sandbox-suspend")
    async def send_suspend(self, inp: SendSuspendInput) -> None:
        handle = self._client.get_workflow_handle(inp.sandbox_id)
        try:
            await handle.execute_update(SandboxWorkflow.suspend, id=inp.update_id)
        except Exception as e:
            raise _wrap_update_error(e)

    @activity.defn(name="send-sandbox-resume")
    async def send_resume(self, inp: SendResumeInput) -> None:
        handle = self._client.get_workflow_handle(inp.sandbox_id)
        try:
            await handle.execute_update(SandboxWorkflow.resume, id=inp.update_id)
        except Exception as e:
            raise _wrap_update_error(e)
