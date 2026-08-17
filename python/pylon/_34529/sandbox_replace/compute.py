"""Compute-provider abstraction and registry.

Python port of `sdk/compute/provider.go` + `sdk/compute/registry.go` from the
Go Sandbox Orchestration Harness. Pure data + a process-global registry; no I/O
here, so this module is safe to import from workflow code.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from enum import IntEnum
from typing import Callable, Dict, Optional, Protocol, Tuple

# Sentinel for idle_timeout_seconds meaning "never auto-suspend".
# Zero means "use the default" (see workflow.IDLE_AUTO_SUSPEND).
NO_IDLE_TIMEOUT: float = -1.0

# Built-in provider type names.
PROVIDER_LOCAL = "local-subprocess"
PROVIDER_AGENTCORE = "aws-agentcore-runtime"


class SandboxPostSnapshotState(IntEnum):
    """State the sandbox is in after Snapshot returns (mirrors the Go enum)."""

    RUNNING = 0  # still running; workflow keeps its lifecycle
    SUSPENDED = 1  # provider paused it as part of snapshotting
    DELETED = 2  # provider destroyed the instance; terminal


@dataclass
class ProviderDetails:
    type: str
    config: Dict[str, str] = field(default_factory=dict)


@dataclass
class ProviderStatus:
    instance_id: str


@dataclass
class ProviderSnapshot:
    snapshot_id: str


@dataclass
class CommandResult:
    stdout: str = ""
    stderr: str = ""
    exit_code: int = 0



@dataclass
class ActivityResult:
    stdout: str = ""
    stderr: str = ""
    exit_code: int = 0



class UnsupportedOperation(Exception):
    """Raised by a provider for an operation it does not support (e.g. native
    suspend/resume). The sandbox workflow catches this and falls back to a
    snapshot-based suspend, mirroring the Go `errors.ErrUnsupported` contract."""


class Provider(Protocol):
    """What a compute backend must implement. `task_queue_name` is the Temporal
    task queue a would-be in-VM worker should poll; it is passed to Start /
    StartFromSnapshot for parity with the Go contract."""

    def start(self, task_queue_name: str) -> ProviderStatus: ...
    def stop(self, status: ProviderStatus) -> None: ...
    def suspend(self, status: ProviderStatus) -> None: ...
    def resume(self, status: ProviderStatus) -> None: ...
    def snapshot(
        self, status: ProviderStatus
    ) -> Tuple[SandboxPostSnapshotState, ProviderSnapshot]: ...
    def start_from_snapshot(
        self, task_queue_name: str, snapshot: ProviderSnapshot
    ) -> ProviderStatus: ...
    def delete_snapshot(self, snapshot: ProviderSnapshot) -> None: ...
    def execute_command(self, status: ProviderStatus, cmd: str) -> CommandResult: ...
    # Re-provision an in-VM worker for an already-provisioned sandbox after its
    # micro-VM was evicted (the self-healing path). Providers without an in-VM
    # worker raise UnsupportedOperation.
    def reboot_worker(self, status: ProviderStatus, task_queue_name: str) -> None: ...


Constructor = Callable[[Dict[str, str]], Provider]

_registry: Dict[str, Constructor] = {}


def register_provider(type_: str, ctor: Constructor) -> None:
    """Register a constructor for a provider type. Called at import time by each
    provider module. Raises if the type is already registered (driver convention)."""
    if type_ in _registry:
        raise RuntimeError(f"compute: provider already registered for type {type_}")
    _registry[type_] = ctor


def is_registered(type_: str) -> bool:
    return type_ in _registry


def lookup(type_: str, config: Optional[Dict[str, str]]) -> Provider:
    ctor = _registry.get(type_)
    if ctor is None:
        raise ValueError(f"no compute provider registered for type {type_!r}")
    provider = ctor(config or {})
    if provider is None:
        raise ValueError(f"compute: constructor for type {type_!r} returned None")
    return provider
