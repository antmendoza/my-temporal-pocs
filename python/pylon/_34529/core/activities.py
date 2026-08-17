"""In-sandbox activities: provision / teardown, and the in-VM work activity.

They run in the worker's activity thread pool (sync `def`), so subprocess/file I/O
is fine here.
"""

from __future__ import annotations

import os
import tempfile
import time
from dataclasses import dataclass

from temporalio import activity
from temporalio.exceptions import ApplicationError

from . import compute
from .compute import ProviderDetails, ProviderStatus

# Import the provider so it self-registers via register_provider() at import time.
from .providers import mockAgentCore as _mock_agentcore  # noqa: F401


def _session_base() -> str:
    # Durable, VM-independent checkpoint store (stand-in for BYO S3/EFS), keyed by
    # the parent-supplied session_ref so it survives sandbox teardown/recreation.
    # Resolved lazily so no restricted call runs at import time (this module is
    # pulled into workflow-sandbox validation).
    return os.path.join(tempfile.gettempdir(), "sandbox-harness", "sessions")


def _lookup(pd: ProviderDetails):
    try:
        return compute.lookup(pd.type, pd.config)
    except Exception as e:  # provider config error → non-retryable
        raise ApplicationError(str(e), type="ProviderConfigError", non_retryable=True)


@dataclass
class StartSandboxInput:
    provider: ProviderDetails
    task_queue_name: str


@dataclass
class StartSandboxOutput:
    status: ProviderStatus


@dataclass
class StopSandboxInput:
    provider: ProviderDetails
    status: ProviderStatus


@dataclass
class ActivityInSandboxInput:
    sleepTimeSeconds: int
    # Durable, VM-independent session key. Resolved to a checkpoint dir that
    # survives sandbox teardown, so a fresh sandbox for the same session resumes.
    session_ref: str


@dataclass
class ActivityInSandboxResult:
    result: str


@activity.defn(name="start-sandbox")
def start_sandbox(inp: StartSandboxInput) -> StartSandboxOutput:
    provider = _lookup(inp.provider)
    return StartSandboxOutput(status=provider.start(inp.task_queue_name))


@activity.defn(name="stop-sandbox")
def stop_sandbox(inp: StopSandboxInput) -> None:
    _lookup(inp.provider).stop(inp.status)


@activity.defn(name="execute-activity-in-sandbox")
def execute_activity_in_sandbox(inp: ActivityInSandboxInput) -> ActivityInSandboxResult:
    # Scheduled on the sandbox's own task queue -> served by the in-VM worker the
    # provider booted. Heartbeats for liveness (so a dead worker is detected via
    # heartbeat_timeout and the failure propagates to the parent), and checkpoints
    # elapsed seconds to the DURABLE session store keyed by session_ref. Because that
    # store is independent of any single sandbox, the fresh sandbox the parent creates
    # after a failure resumes from here instead of restarting from zero.
    info = activity.info()
    durable_dir = os.path.join(_session_base(), inp.session_ref)
    os.makedirs(durable_dir, exist_ok=True)
    progress_file = os.path.join(durable_dir, "progress")
    try:
        with open(progress_file) as f:
            elapsed = int(f.read().strip() or "0")
    except FileNotFoundError:
        elapsed = 0
    resumed_from = elapsed
    while elapsed < inp.sleepTimeSeconds:
        time.sleep(1)
        elapsed += 1
        with open(progress_file, "w") as f:
            f.write(str(elapsed))
        activity.heartbeat(elapsed)
        print(f"progress {elapsed}/{inp.sleepTimeSeconds}s on task queue {info.task_queue}")
    return ActivityInSandboxResult(
        result=(
            f"completed {inp.sleepTimeSeconds}s of work on task queue "
            f"{info.task_queue} (resumed from {resumed_from}s)"
        )
    )
