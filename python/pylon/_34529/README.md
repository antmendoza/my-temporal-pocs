# 34529 — sandbox worker self-healing (replace model)

Ticket 34529: Temporal workers on AWS Bedrock **AgentCore Runtime**, where the worker
runs *inside* an ephemeral micro-VM. Goal: **detect when the micro-VM goes down and
bring the work back up on a new one.**

## Layout

- `core/` — workflows + activities + SDK:
  - `parent_workflow.py` — `ParentRecreateWorkflow`: creates a sandbox, runs work, and on
    a lost micro-VM **creates a new sandbox** and retries. Work resumes from a durable
    checkpoint, so it finishes even though no single VM lives long enough.
  - `sandbox_workflow.py` — `SandboxWorkflow`, the child (workflow ID == sandbox ID):
    `init` provisions the VM (booting the in-VM worker), `execute_activity` runs the work
    on `sandbox-<id>`, `stop` tears it down. A lost VM surfaces as an activity timeout
    tagged `SANDBOX_WORKER_LOST`, which propagates to the parent.
  - `activities.py`, `dispatch.py`, `sandbox.py`, `compute.py`, `registration.py`,
    `providers/` — the SDK + the `mock-agentcore` provider, which boots a real in-VM
    worker subprocess and evicts it after `max_lifetime` (no AWS/boto3; runs locally).
- `workers/` — `orchestrator_worker.py` (always-on: runs both workflows + provisioning)
  and `vm_worker.py` (the ephemeral in-VM worker, one per sandbox).
- `starter/main.py` — starts `ParentRecreateWorkflow`.

## Run it

Three terminals:

```bash
temporal server start-dev
```


```bash
.venv/bin/python -u -m workers.orchestrator_worker
```


```bash
.venv/bin/python -m starter.main
```

Expected result:

```
completed 20s of work on task queue sandbox-<id> (resumed from 14s) [completed with 3 sandbox(es)]
```

`resumed from …s` and `completed with N sandbox(es)` show the work survived the micro-VM
evictions. The orchestrator worker's log shows the `booted … / evicted …` cycle — a new
`sandbox-<id>` per recreated VM.

## Knobs

In `core/parent_workflow.py`: `WORK_SECONDS` (default 20), `VM_MAX_LIFETIME_SECONDS`
(default 8), `MAX_SANDBOXES` (default 6). Set `WORK_SECONDS` above `VM_MAX_LIFETIME_SECONDS`
to force recreations.
