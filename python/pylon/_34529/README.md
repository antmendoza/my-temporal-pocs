# 34529 — Python port of the Sandbox Orchestration Harness

A faithful Python port of the core of the Go
[Sandbox Orchestration Harness](https://github.com/temporal-community/sandbox-orchestration-harness)
(`~/dev/temporal/sandbox-orchestration-harness`), built for ticket 34529
(Temporal workers on AWS AgentCore runtime — dynamic worker lifecycle).

It reproduces the pattern the customer conversation is grounded in, in Python,
and runs locally against `temporal server start-dev` with **no cloud credentials**.

## What it ports

The two-workflow pattern from the Go SDK:

- **`SandboxWorkflow`** (`sandbox/workflow.py`) — a long-lived child workflow whose
  workflow ID *is* the sandbox ID. Registers `sandbox-init`, `sandbox-execute-command`,
  `sandbox-suspend`, `sandbox-resume` updates, a `sandbox-state` query, and a
  `sandbox-stop` signal. Owns the lifecycle state machine
  (Pending → Running → Suspended → …) and the idle auto-suspend.
- **Update-as-activity dispatch** (`sandbox/dispatch.py`) — a workflow can't call
  `UpdateWorkflow` on another workflow, so each operation the parent invokes is an
  activity that calls `execute_update` against the sandbox, with a deterministic,
  idempotent `update_id` (`workflow.uuid4()`) so replays dedupe.
- **In-sandbox activities** (`sandbox/activities.py`) — look up the provider and call
  it (`start` / `stop` / `execute_command` / `snapshot` / …). `UnsupportedOperation`
  is surfaced as a non-retryable `ErrUnsupported` so the workflow can fall back.
- **Parent handle** (`sandbox/sandbox.py`) — `new_sandbox(...)` + `Sandbox.execute_command`
  / `suspend` / `resume` / `stop`, used from inside the agent (parent) workflow.
- **Providers** (`sandbox/providers/`):
  - `local.py` — **runnable** local provider: each sandbox is a temp working dir,
    commands run via `/bin/sh -c`. Like Modal it reports native suspend/resume as
    unsupported, so the **snapshot-based suspend fallback** is exercised (snapshot =
    copy the dir, resume = restore it), and files survive an idle-suspend/resume cycle.
  - `agentcore.py` — **stub** documenting the ticket-34529 gap (see below).

### Deliberately omitted (vs. the Go SDK)

To keep the port focused on the customer's worker-lifecycle interest: user-initiated
`Snapshot`/`DeleteSnapshot` updates and the snapshot-fork flow, `Ref`/`AttachToSandbox`
sharing, and `WithCleanup(CleanupDisabled)` detached lifetimes (the enum exists but only
the default REQUEST_CANCEL path is used). The internal snapshot machinery *is* present
because the suspend fallback needs it.

## The ticket-34529 gap (why the AgentCore provider is a stub)

The Go harness drives AgentCore via the AWS API (`InvokeAgentRuntime`) with the Temporal
worker running **outside** the micro-VM — "AgentCore as a sandbox provider". The customer
wants the worker **inside** the AgentCore micro-VM (1:1 session ↔ VM ↔ worker ↔ task,
workers provisioned on demand and torn down when idle). That is closer to
Serverless / ephemeral Workers hosted on AgentCore than to this provider path.
`sandbox/providers/agentcore.py` records that boundary; the in-VM worker path is not
wired end-to-end. See `../../../notes-for-tickets/34529.md`.

## Run it

```bash
# 1. deps (uv)
uv venv --python 3.12 && uv pip install "temporalio>=1.15,<2"
```

```bash
# 2. dev server (separate terminal)
temporal server start-dev
```

```bash
# 3. worker (separate terminal)
.venv/bin/python -m auto_suspend.worker
```

```bash
# 4. run the example
.venv/bin/python -m auto_suspend.starter
```

`auto_suspend` is the Python port of the Go `examples/auto-suspend`: it writes a file,
waits past the 30s idle timeout (sandbox auto-suspends via snapshot, tearing down the
working dir), then runs another command that transparently resumes from the snapshot and
confirms the file is still there. Expected output — identical listings before and after:

```
before suspend: -rw-r--r-- 1 ... session/persist.txt
after  suspend: -rw-r--r-- 1 ... session/persist.txt
```

Inspect the `SandboxWorkflow` child history and you'll see the real path:
`start-sandbox → 3× execute-command → (idle timer) → suspend-sandbox (unsupported) →
snapshot-sandbox → stop-sandbox → start-sandbox-from-snapshot → delete-snapshot →
execute-command → stop-sandbox`.

## Notes for a Python SDK reader

- The provider **registry** is populated by import side effects in the *activity* worker
  process; it is not reliably visible inside the workflow sandbox, so provider-type
  validity is enforced by the init activity's `compute.lookup`, not a workflow validator.
- Idle auto-suspend runs in the **main workflow coroutine** via
  `wait_condition(timeout=…)`, not a detached `asyncio` task — a fire-and-forget task
  spawned inside an update handler races the next update and can be dropped before it
  schedules its activity.
- Activity failures raise `ActivityError`; the `ErrUnsupported` fallback is detected on
  `err.cause`, not on the raised exception directly.
