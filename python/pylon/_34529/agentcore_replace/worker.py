"""The in-VM Temporal worker — the process the mock AgentCore provider boots
inside each micro-VM.

Unlike `auto_suspend/worker.py` (the always-on worker that runs the orchestrating
AutoSuspendWorkflow on a fixed task queue), this worker is ephemeral and keyed to a
single sandbox session: it polls the sandbox's own task queue (`sandbox-<id>`, passed
by the provider on start) and lives only as long as the micro-VM. When the VM is
evicted (idle auto-suspend, 8h max lifetime), the provider terminates this process;
a resume boots a fresh one from the snapshot.

It registers the sandbox SDK's workflows/activities so the session's work can be
routed to it. Run standalone:

    python -m agentcore.worker <task-queue-name>

or let `sandbox/providers/mockAgentCore.py` launch it.
"""

from __future__ import annotations

import asyncio
import os
import sys
from concurrent.futures import ThreadPoolExecutor

from temporalio.client import Client
from temporalio.worker import Worker

from sandbox_replace.registration import register


def _task_queue() -> str:
    if len(sys.argv) > 1 and sys.argv[1].strip():
        return sys.argv[1].strip()
    env = os.environ.get("TEMPORAL_TASK_QUEUE", "").strip()
    if env:
        return env
    raise SystemExit(
        "usage: python -m agentcore.worker <task-queue-name> "
        "(or set TEMPORAL_TASK_QUEUE)"
    )


async def main() -> None:
    task_queue = _task_queue()
    client = await Client.connect(
        os.environ.get("TEMPORAL_ADDRESS", "localhost:7233")
    )
    sandbox_workflows, sandbox_activities = register(client)

    with ThreadPoolExecutor(max_workers=8) as executor:
        worker = Worker(
            client,
            task_queue=task_queue,
            workflows=sandbox_workflows,
            activities=sandbox_activities,
            activity_executor=executor,
        )
        print(
            f"[in-vm-worker] running on task queue {task_queue!r} (ctrl-c to exit)",
            flush=True,
        )
        await worker.run()


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        pass
