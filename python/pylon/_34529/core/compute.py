"""Compute-provider abstraction and registry.

Pure data + a process-global registry; no I/O here, so this module is safe to
import from workflow code.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Callable, Dict, Optional, Protocol

# Raised (as an ApplicationError type) by SandboxWorkflow.execute_activity when the
# in-VM worker / micro-VM is gone (activity heartbeat_timeout or schedule_to_start
# timeout).
SANDBOX_WORKER_LOST = "SandboxWorkerLost"


@dataclass
class ProviderDetails:
    type: str
    config: Dict[str, str] = field(default_factory=dict)


@dataclass
class ProviderStatus:
    instance_id: str


class Provider(Protocol):
    """A compute backend. `task_queue_name` is the queue the in-VM worker polls."""

    def start(self, task_queue_name: str) -> ProviderStatus: ...
    def stop(self, status: ProviderStatus) -> None: ...


Constructor = Callable[[Dict[str, str]], Provider]

_registry: Dict[str, Constructor] = {}


def register_provider(type_: str, ctor: Constructor) -> None:
    """Register a constructor for a provider type. Called at import time by the
    provider module."""
    if type_ in _registry:
        raise RuntimeError(f"compute: provider already registered for type {type_}")
    _registry[type_] = ctor


def lookup(type_: str, config: Optional[Dict[str, str]]) -> Provider:
    ctor = _registry.get(type_)
    if ctor is None:
        raise ValueError(f"no compute provider registered for type {type_!r}")
    return ctor(config or {})
