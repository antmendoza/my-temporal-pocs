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
registered. Unlike `auto_suspend/worker.py` (the always-on worker that runs the
orchestrating `AutoSuspendWorkflow` on a fixed queue), this one lives only as long as
its micro-VM.

Run it standalone:

```bash
.venv/bin/python -m agentcore.worker sandbox-<some-id>
```

## Run the demo against the mock

The `auto_suspend` example defaults to `local-subprocess`. To watch the in-VM worker
lifecycle, point it at the mock — one line in `auto_suspend/workflow.py`:

```python
sbx = await new_sandbox(
    ProviderDetails(type=PROVIDER_MOCK_AGENTCORE, config={"image": "ubuntu:26.04"}),
    idle_timeout_seconds=IDLE_TIMEOUT_SECONDS,
)
```

(and import it: `from sandbox.providers.mockAgentCore import PROVIDER_MOCK_AGENTCORE`)

Then, in three terminals:

```bash
temporal server start-dev                       # 1. dev server
.venv/bin/python -u -m auto_suspend.worker      # 2. always-on orchestrator worker
.venv/bin/python -m auto_suspend.starter        # 3. run the example
```

The starter prints the before/after listing (identical — the file survives). The
orchestrator worker's stdout shows the micro-VM lifecycle:

```
[mock-agentcore] booted micro-VM worker pid=18608 task_queue='sandbox-<id>' instance=/…/sbx-…
[in-vm-worker] running on task queue 'sandbox-<id>' (ctrl-c to exit)
…idle → snapshot suspend fallback → stop…
[mock-agentcore] evicted micro-VM worker pid=18608 instance=/…/sbx-…
[mock-agentcore] booted micro-VM worker pid=18925 task_queue='sandbox-<id>' instance=/…/sbx-…   # resume from snapshot
[mock-agentcore] evicted micro-VM worker pid=18925 instance=/…/sbx-…                            # final stop
```

Use `-u` (unbuffered) on the orchestrator worker so those lines surface in real time.

## Caveats — it's a mock

- The in-VM worker boots and polls `sandbox-<id>`, but the auto-suspend demo still runs
  the sandbox activities on the orchestrator's queue, so the in-VM worker sits idle.
  It proves the **boot/evict/reboot lifecycle**, not task routing to the in-VM worker.
- Subprocesses are children of the orchestrator worker process. If that process dies
  without calling `stop`, a child worker can linger — acceptable for a local mock.
- Real AgentCore adds pieces this mock omits: the `InvokeAgentRuntime` boot trigger
  (the Serverless-Workers webhook role), managed session storage, and the `HealthyBusy`
  `/ping` keep-alive. See `../../../notes-for-tickets/34529.md` and the `temporal-ai`
  wiki (`agentcore-runtime-sessions.md`).
