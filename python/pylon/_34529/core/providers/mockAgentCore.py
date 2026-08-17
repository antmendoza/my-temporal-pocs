"""Mock AWS Bedrock AgentCore Runtime provider — the in-VM-worker model.

Boots a real Temporal worker *inside* each sandbox (a subprocess running
`workers.vm_worker`) polling the sandbox's own task queue, and can evict it
after `max_lifetime` seconds — the stand-in for AgentCore's hard max-lifetime
eviction. No AWS credentials or boto3; runs locally against `temporal server start-dev`.
"""

from __future__ import annotations

import os
import shutil
import subprocess
import sys
import tempfile
import threading
import uuid
from pathlib import Path
from typing import Dict

from ..compute import ProviderStatus, register_provider

PROVIDER_MOCK_AGENTCORE = "mock-agentcore-runtime"

BASE = os.path.join(tempfile.gettempdir(), "sandbox-harness")

# Project root so the in-VM worker subprocess resolves `-m workers.vm_worker`.
_PROJECT_ROOT = Path(__file__).resolve().parents[2]

# instance_id (workdir) -> in-VM worker subprocess; and -> pending eviction timer.
# Module-level so start() and a later stop() (separate activity calls in the same
# worker process) share them.
_workers: Dict[str, subprocess.Popen] = {}
_timers: Dict[str, threading.Timer] = {}


def _new_dir(prefix: str) -> str:
    os.makedirs(BASE, exist_ok=True)
    path = os.path.join(BASE, f"{prefix}-{uuid.uuid4().hex}")
    os.makedirs(path, exist_ok=True)
    return path


def _boot_in_vm_worker(
    instance_id: str, task_queue_name: str, max_lifetime: float = 0.0
) -> None:
    """Boot the in-VM worker subprocess. If `max_lifetime` > 0, arm a timer that
    evicts it after that many seconds (the mock's hard max-lifetime eviction)."""
    env = dict(os.environ)
    env["PYTHONPATH"] = os.pathsep.join(
        [str(_PROJECT_ROOT), env.get("PYTHONPATH", "")]
    ).rstrip(os.pathsep)
    proc = subprocess.Popen(
        [sys.executable, "-m", "workers.vm_worker", task_queue_name],
        cwd=str(_PROJECT_ROOT),
        env=env,
    )
    _workers[instance_id] = proc
    print(
        f"[mock-agentcore] booted micro-VM worker pid={proc.pid} "
        f"task_queue={task_queue_name!r} instance={instance_id}",
        flush=True,
    )
    if max_lifetime > 0:
        _arm_lifetime_timer(instance_id, max_lifetime)


def _arm_lifetime_timer(instance_id: str, max_lifetime: float) -> None:
    old = _timers.pop(instance_id, None)
    if old is not None:
        old.cancel()
    timer = threading.Timer(max_lifetime, _evict_in_vm_worker, args=(instance_id,))
    timer.daemon = True
    _timers[instance_id] = timer
    timer.start()


def _evict_in_vm_worker(instance_id: str) -> None:
    """Simulate the micro-VM being evicted: hard-kill its in-VM worker."""
    timer = _timers.pop(instance_id, None)
    if timer is not None:
        timer.cancel()
    proc = _workers.pop(instance_id, None)
    if proc is None:
        return
    proc.kill()  # SIGKILL: no graceful shutdown, mirroring a hard VM eviction
    try:
        proc.wait(timeout=10)
    except subprocess.TimeoutExpired:
        pass
    print(
        f"[mock-agentcore] evicted micro-VM worker pid={proc.pid} instance={instance_id}",
        flush=True,
    )


class MockAgentCoreProvider:
    def __init__(self, config: Dict[str, str]):
        # Seconds before the mock evicts the in-VM worker (0 = never). Stand-in for
        # AgentCore's hard max-lifetime; set below the work duration to force a loss.
        self._max_lifetime = float(config.get("max_lifetime", "0") or "0")

    def start(self, task_queue_name: str) -> ProviderStatus:
        workdir = _new_dir("sbx")
        _boot_in_vm_worker(workdir, task_queue_name, self._max_lifetime)
        return ProviderStatus(instance_id=workdir)

    def stop(self, status: ProviderStatus) -> None:
        _evict_in_vm_worker(status.instance_id)
        shutil.rmtree(status.instance_id, ignore_errors=True)


register_provider(PROVIDER_MOCK_AGENTCORE, lambda config: MockAgentCoreProvider(config))
