"""A runnable local provider: each sandbox is a temp working directory, commands
run via `/bin/sh -c` with that dir as cwd. No cloud credentials needed.

Like the Go Modal provider, it reports native suspend/resume as *unsupported* so
the sandbox workflow exercises the snapshot-based suspend fallback: snapshot =
copy the working dir, resume = restore it into a fresh dir. Files written under
the sandbox dir therefore survive an idle auto-suspend + resume cycle.
"""

from __future__ import annotations

import os
import shutil
import subprocess
import tempfile
import uuid
from typing import Dict, Tuple

from ..compute import (
    PROVIDER_LOCAL,
    CommandResult,
    ProviderSnapshot,
    ProviderStatus,
    SandboxPostSnapshotState,
    UnsupportedOperation,
    register_provider,
)

BASE = os.path.join(tempfile.gettempdir(), "sandbox-harness")


def _new_dir(prefix: str) -> str:
    os.makedirs(BASE, exist_ok=True)
    path = os.path.join(BASE, f"{prefix}-{uuid.uuid4().hex}")
    os.makedirs(path, exist_ok=True)
    return path


class LocalSubprocessProvider:
    def __init__(self, config: Dict[str, str]):
        # `image` is unused locally; kept for config parity with cloud providers.
        self._image = config.get("image", "")

    def start(self, task_queue_name: str) -> ProviderStatus:
        workdir = _new_dir("sbx")
        # A real in-VM worker would poll this task queue; recorded for parity
        # with the harness's TEMPORAL_TASK_QUEUE scaffolding.
        with open(os.path.join(workdir, ".task_queue"), "w") as f:
            f.write(task_queue_name)
        return ProviderStatus(instance_id=workdir)

    def stop(self, status: ProviderStatus) -> None:
        shutil.rmtree(status.instance_id, ignore_errors=True)

    def suspend(self, status: ProviderStatus) -> None:
        raise UnsupportedOperation("local: suspend unsupported (snapshot fallback)")

    def resume(self, status: ProviderStatus) -> None:
        raise UnsupportedOperation("local: resume unsupported (snapshot fallback)")

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
        return ProviderStatus(instance_id=workdir)

    def delete_snapshot(self, snapshot: ProviderSnapshot) -> None:
        shutil.rmtree(snapshot.snapshot_id, ignore_errors=True)

    def reboot_worker(self, status: ProviderStatus, task_queue_name: str) -> None:
        raise UnsupportedOperation("local: no in-VM worker to reboot")

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


register_provider(PROVIDER_LOCAL, lambda config: LocalSubprocessProvider(config))
