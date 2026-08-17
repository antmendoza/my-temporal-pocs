"""AWS Bedrock AgentCore Runtime provider — STUB.

Mirrors the shape of the Go AgentCore provider but is intentionally not wired
end-to-end, to make the ticket-34529 gap explicit:

  The Go harness drives AgentCore via the AWS API (InvokeAgentRuntime) with the
  Temporal worker running OUTSIDE the micro-VM ("AgentCore as a sandbox provider").
  The customer wants the worker INSIDE the AgentCore micro-VM — a 1:1
  session <-> micro-VM <-> worker <-> task mapping, workers provisioned
  dynamically and torn down when idle. That model is closer to
  Serverless / ephemeral Workers hosted on AgentCore than to this provider path.

See notes-for-tickets/34529.md. Every method raises UnsupportedOperation so the
stub can be registered and inspected without pulling in boto3 or AWS creds.
"""

from __future__ import annotations

from typing import Dict, Tuple

from ..compute import (
    PROVIDER_AGENTCORE,
    CommandResult,
    ProviderSnapshot,
    ProviderStatus,
    SandboxPostSnapshotState,
    UnsupportedOperation,
    register_provider,
)


class AgentCoreRuntimeProvider:
    def __init__(self, config: Dict[str, str]):
        self._config = config

    def _todo(self, op: str):
        raise UnsupportedOperation(
            f"agentcore: {op} not implemented in this port — see ticket 34529 "
            "(in-VM worker vs API-driven sandbox exec)"
        )

    def start(self, task_queue_name: str) -> ProviderStatus:
        self._todo("start")

    def stop(self, status: ProviderStatus) -> None:
        self._todo("stop")

    def suspend(self, status: ProviderStatus) -> None:
        self._todo("suspend")

    def resume(self, status: ProviderStatus) -> None:
        self._todo("resume")

    def snapshot(
        self, status: ProviderStatus
    ) -> Tuple[SandboxPostSnapshotState, ProviderSnapshot]:
        self._todo("snapshot")

    def start_from_snapshot(
        self, task_queue_name: str, snapshot: ProviderSnapshot
    ) -> ProviderStatus:
        self._todo("start_from_snapshot")

    def delete_snapshot(self, snapshot: ProviderSnapshot) -> None:
        self._todo("delete_snapshot")

    def execute_command(self, status: ProviderStatus, cmd: str) -> CommandResult:
        self._todo("execute_command")


register_provider(PROVIDER_AGENTCORE, lambda config: AgentCoreRuntimeProvider(config))
