"""Worker registration helper.

Returns the workflows and activities the sandbox SDK needs registered on a worker.
The DispatchActivities instance is bound to the given client so the `send-sandbox-*`
activities can call execute_update.
"""

from __future__ import annotations

from typing import List, Tuple

from temporalio.client import Client

from .sandbox_workflow import SandboxWorkflow

from . import activities
from .dispatch import DispatchActivities


def register(client: Client) -> Tuple[List[type], list]:
    d = DispatchActivities(client)
    workflows: List[type] = [SandboxWorkflow]
    acts = [
        activities.start_sandbox,
        activities.stop_sandbox,
        activities.execute_activity_in_sandbox,
        d.send_init,
        d.send_execute_activity,
    ]
    return workflows, acts
