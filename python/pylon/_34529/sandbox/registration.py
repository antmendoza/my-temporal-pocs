"""Worker registration helper (port of `sandbox.Register`).

Returns the workflows and activities the sandbox SDK needs registered on a
worker. The DispatchActivities instance is bound to the given client so the
`send-sandbox-*` activities can call execute_update.
"""

from __future__ import annotations

from typing import List, Tuple

from temporalio.client import Client

from . import activities
from .dispatch import DispatchActivities
from .workflow import SandboxWorkflow


def register(client: Client) -> Tuple[List[type], list]:
    d = DispatchActivities(client)
    workflows: List[type] = [SandboxWorkflow]
    acts = [
        # in-sandbox lifecycle activities
        activities.start_sandbox,
        activities.stop_sandbox,
        activities.suspend_sandbox,
        activities.resume_sandbox,
        activities.execute_command,
        activities.snapshot_sandbox,
        activities.start_sandbox_from_snapshot,
        activities.delete_snapshot,
        # client-side dispatch activities
        d.send_init,
        d.send_execute_command,
        d.send_suspend,
        d.send_resume,
    ]
    return workflows, acts
