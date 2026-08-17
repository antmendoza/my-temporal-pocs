"""Mock AWS Bedrock AgentCore Runtime provider — the in-VM-worker model.

Where the real `agentcore.py` is a stub (and the Go harness drives AgentCore from
*outside* the micro-VM via `InvokeAgentRuntime`), this mock demonstrates the model
the ticket-34529 customer actually wants: a Temporal worker running **inside** the
micro-VM, provisioned on demand per session and torn down when the VM goes away.

It reuses the local provider's mechanics for the compute itself (each sandbox is a
temp working dir; commands run via `/bin/sh -c`; suspend is unsupported so the
snapshot fallback runs). The one thing it adds is the point of the exercise:

  * `start` / `start_from_snapshot` boot a real `agentcore/worker.py` subprocess
    ("the micro-VM's in-VM worker"), polling the sandbox's own task queue.
  * `stop` terminates that subprocess ("the micro-VM is evicted").

So the idle auto-suspend cycle (snapshot → stop → start-from-snapshot) visibly kills
the in-VM worker and reboots it — exactly the lifecycle the customer needs to survive.
No AWS credentials or boto3 required; runs locally against `temporal server start-dev`.
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
from typing import Dict, Tuple

from ..compute import (
    CommandResult,
    ProviderSnapshot,
    ProviderStatus,
    SandboxPostSnapshotState,
    UnsupportedOperation,
    register_provider,
)

# Distinct provider type so it can coexist with the `local` and stub `agentcore`
# providers in the same registry. Select it with ProviderDetails(type=...).
PROVIDER_MOCK_AGENTCORE = "mock-agentcore-runtime"

BASE = os.path.join(tempfile.gettempdir(), "sandbox-harness")

# Project root (_34529/), so the in-VM worker subprocess can be launched with
# `python -m agentcore.worker` regardless of the main worker's cwd.
_PROJECT_ROOT = Path(__file__).resolve().parents[2]

# instance_id (workdir) -> in-VM worker subprocess. Module-level so start() and a
# later stop() — which run as separate activity calls in the same worker process —
# share it.
_workers: Dict[str, subprocess.Popen] = {}
# instance_id -> pending max-lifetime eviction timer (see _arm_lifetime_timer).
_timers: Dict[str, threading.Timer] = {}


def _new_dir(prefix: str) -> str:
    os.makedirs(BASE, exist_ok=True)
    path = os.path.join(BASE, f"{prefix}-{uuid.uuid4().hex}")
    os.makedirs(path, exist_ok=True)
    return path


def _boot_in_vm_worker(
    instance_id: str, task_queue_name: str, max_lifetime: float = 0.0
) -> None:
    """Simulate the AgentCore micro-VM booting a Temporal worker inside itself.

    If `max_lifetime` > 0, arm a timer that evicts the worker after that many
    seconds — the mock's stand-in for AgentCore's hard max-lifetime eviction."""
    env = dict(os.environ)
    # Prepend the project root so `-m agentcore.worker` resolves even if the
    # spawning process was started from elsewhere.
    env["PYTHONPATH"] = os.pathsep.join(
        [str(_PROJECT_ROOT), env.get("PYTHONPATH", "")]
    ).rstrip(os.pathsep)
    proc = subprocess.Popen(
        [sys.executable, "-m", "agentcore_replace.worker", task_queue_name],
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
    """Simulate the micro-VM being evicted: hard-kill its in-VM worker. The workdir
    (managed session storage) is left intact, so a re-provisioned worker can resume."""
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
        # `image` is unused in the mock; kept for config parity with the real provider.
        self._image = config.get("image", "")
        # Seconds before the mock evicts the in-VM worker (0 = never). Stand-in for
        # AgentCore's hard max-lifetime; set it below the work duration to exercise
        # the self-healing path.
        self._max_lifetime = float(config.get("max_lifetime", "0") or "0")

    def start(self, task_queue_name: str) -> ProviderStatus:
        workdir = _new_dir("sbx")
        with open(os.path.join(workdir, ".task_queue"), "w") as f:
            f.write(task_queue_name)
        _boot_in_vm_worker(workdir, task_queue_name, self._max_lifetime)
        return ProviderStatus(instance_id=workdir)

    def reboot_worker(self, status: ProviderStatus, task_queue_name: str) -> None:
        # Re-provision after an eviction: boot a fresh in-VM worker against the
        # existing workdir (session storage persisted) and re-arm the lifetime timer.
        _boot_in_vm_worker(status.instance_id, task_queue_name, self._max_lifetime)

    def stop(self, status: ProviderStatus) -> None:
        _evict_in_vm_worker(status.instance_id)
        shutil.rmtree(status.instance_id, ignore_errors=True)

    def suspend(self, status: ProviderStatus) -> None:
        raise UnsupportedOperation("mock-agentcore: suspend unsupported (snapshot fallback)")

    def resume(self, status: ProviderStatus) -> None:
        raise UnsupportedOperation("mock-agentcore: resume unsupported (snapshot fallback)")

    def snapshot(
        self, status: ProviderStatus
    ) -> Tuple[SandboxPostSnapshotState, ProviderSnapshot]:
        os.makedirs(BASE, exist_ok=True)
        snap = os.path.join(BASE, f"snap-{uuid.uuid4().hex}")
        shutil.copytree(status.instance_id, snap)
        return SandboxPostSnapshotState.RUNNING, ProviderSnapshot(snapshot_id=snap)

    def start_from_snapshot(
        self, task_queue_name: str, snapshot: ProviderSnapshot
    ) -> ProviderStatus:
        workdir = os.path.join(BASE, f"sbx-{uuid.uuid4().hex}")
        shutil.copytree(snapshot.snapshot_id, workdir)
        with open(os.path.join(workdir, ".task_queue"), "w") as f:
            f.write(task_queue_name)
        _boot_in_vm_worker(workdir, task_queue_name, self._max_lifetime)
        return ProviderStatus(instance_id=workdir)

    def delete_snapshot(self, snapshot: ProviderSnapshot) -> None:
        shutil.rmtree(snapshot.snapshot_id, ignore_errors=True)

    def execute_command(self, status: ProviderStatus, cmd: str) -> CommandResult:
        proc = subprocess.run(
            ["/bin/sh", "-c", cmd],
            cwd=status.instance_id,
            capture_output=True,
            text=True,
        )
        return CommandResult(
            stdout=proc.stdout, stderr=proc.stderr, exit_code=proc.returncode
        )


register_provider(PROVIDER_MOCK_AGENTCORE, lambda config: MockAgentCoreProvider(config))
