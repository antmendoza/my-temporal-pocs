# Mock AgentCore provider — the in-VM-worker model

A runnable mock of AWS Bedrock **AgentCore Runtime** that demonstrates the model the
ticket-34529 customer actually wants: a Temporal worker running **inside** each
micro-VM, provisioned on demand per session and torn down when the VM goes away.

It complements the two existing providers:

| Provider | Type string | Worker location | Purpose |
|---|---|---|---|
| `local.py` | `local-subprocess` | none (compute runs in-process) | runnable local demo |
| `agentcore.py` | `aws-agentcore-runtime` | outside the VM (API-driven) | **stub** — documents the gap |
| `mockAgentCore.py` | `mock-agentcore-runtime` | **inside the VM** | this file — the in-VM-worker model |

## Why this exists

The Go harness (and the `agentcore.py` stub) drive AgentCore from *outside* the
micro-VM via `InvokeAgentRuntime` — "AgentCore as a sandbox provider". The customer's
model is the inverse: a **1:1 session ↔ micro-VM ↔ worker ↔ task** mapping where the
worker lives inside the VM, boots when the session starts, and dies when the VM is
evicted (idle, or the 8h max lifetime). See `../../../notes-for-tickets/34529.md`.

This mock makes that lifecycle observable locally, with no AWS credentials or boto3.

## What it does

`MockAgentCoreProvider` reuses the local provider's compute mechanics — each sandbox
is a temp working dir, commands run via `/bin/sh -c`, and native suspend/resume are
reported unsupported so the snapshot-based fallback runs. The one thing it adds is the
point of the exercise:

- **`start` / `start_from_snapshot`** boot a real `agentcore/worker.py` subprocess —
  the "in-VM worker" — polling the sandbox's own task queue (`sandbox-<id>`, passed in
  by the SandboxWorkflow). The `Popen` is tracked in a module-level
  `instance_id → process` map.
- **`stop`** terminates that subprocess ("the micro-VM is evicted") and removes the
  workdir.

Because the harness's idle auto-suspend runs as *snapshot → stop → start-from-snapshot*,
the in-VM worker is visibly **killed with the VM and rebooted from the snapshot** on the
next command — exactly the lifecycle the customer needs to survive.

### The in-VM worker (`agentcore/worker.py`)

An ephemeral worker keyed to a single session. It reads the task queue from `argv[1]`
(or `TEMPORAL_TASK_QUEUE`), connects, and polls only that queue with the sandbox SDK
registered. Unlike the always-on orchestrator worker (`self_healing/worker.py` /
`general_purpose_worker/worker.py`), this one lives only as long as its micro-VM.

Run it standalone:

```bash
.venv/bin/python -m agentcore.worker sandbox-<some-id>
```

## Run the demo against the mock

Two examples use this mock to solve the ticket-34529 goal — detect a downed micro-VM and
bring the work back up. Both route the work activity to the in-VM worker on `sandbox-<id>`
and set a short `max_lifetime` so the VM is evicted mid-task:

- `self_healing/` — the child `SandboxWorkflow` reboots the worker in place.
- `general_purpose_worker/` — the parent creates a new sandbox/VM.

Run commands and expected output are in **`README_self_healing.md`**. The orchestrator
worker's stdout shows the micro-VM lifecycle (`-u` for unbuffered, real-time lines):

```
[mock-agentcore] booted micro-VM worker pid=18608 task_queue='sandbox-<id>' instance=/…/sbx-…
[in-vm-worker] running on task queue 'sandbox-<id>' (ctrl-c to exit)
[mock-agentcore] evicted micro-VM worker pid=18608 instance=/…/sbx-…   # max_lifetime hit mid-task
[mock-agentcore] booted micro-VM worker pid=18925 task_queue='sandbox-<id>' instance=/…/sbx-…   # re-provisioned
```

## Caveats — it's a mock

- Subprocesses are children of the orchestrator worker process. If that process dies
  without calling `stop`, a child worker can linger — acceptable for a local mock.
- Real AgentCore adds pieces this mock omits: the `InvokeAgentRuntime` boot trigger
  (the Serverless-Workers webhook role), managed session storage, and the `HealthyBusy`
  `/ping` keep-alive. See `../../../notes-for-tickets/34529.md` and the `temporal-ai`
  wiki (`agentcore-runtime-sessions.md`).
