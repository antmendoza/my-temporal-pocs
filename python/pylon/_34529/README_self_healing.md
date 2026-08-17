# In-VM worker self-healing

Two ways a workflow keeps work running when the sandbox micro-VM is evicted mid-task.
Both use the mock AgentCore provider, which boots a real Temporal worker *inside* each
sandbox (`agentcore/worker.py`) and can evict it after `max_lifetime` seconds.

## How the two workflows interact

```
parent workflow ──new_sandbox()──▶ child SandboxWorkflow (id == sandbox id)
      │                                   │ start-sandbox activity
      │                                   ▼
      │                            boots in-VM worker on task queue  sandbox-<id>
      │
      └─sbx.run_activity(n)─▶ dispatch activity ─update─▶ child.execute_activity
                                                              │ schedules activity
                                                              ▼  ON  sandbox-<id>
                                                       in-VM worker runs it
```

- The **parent** only issues intent (`run_activity`); it never touches the sandbox.
- The **child** owns the sandbox and schedules the work activity **on the sandbox's own
  task queue**, so the in-VM worker runs it.
- The activity **heartbeats** (so a dead VM is detected via `heartbeat_timeout`) and
  **checkpoints progress**, so a restart resumes instead of starting over.

## Two recovery models

| | `self_healing/` — **repair** | `parent_recreate/` — **replace** |
|---|---|---|
| Who recovers | the child, in place | the parent |
| On timeout | child reboots the worker, retries | error propagates → parent makes a **new** child sandbox |
| Sandbox id | same across retries | new id each time |
| Progress kept in | the sandbox workdir | a durable, VM-independent `session_ref` store |

`replace` matches real AgentCore better: a re-provision is a genuinely new micro-VM, and
state comes from a durable mount, not the VM.

## Test it

```bash
# terminal 1
temporal server start-dev

# terminal 2 — repair model
.venv/bin/python -m self_healing.worker
# terminal 3
.venv/bin/python -m self_healing.starter
```

```bash
# terminal 2 — replace model (instead of the above)
.venv/bin/python -m parent_recreate.worker
# terminal 3
.venv/bin/python -m parent_recreate.starter
```

Both do 20s of work with an 8s VM lifetime, so the VM is evicted ~2× before the work
finishes. Expected result (note `resumed from …s` — work survived the evictions):

```
# self_healing
completed 20s of work on task queue sandbox-<id> (resumed from 14s)

# parent_recreate
completed 20s of work on task queue sandbox-<id> (resumed from 14s) [completed with 3 sandbox(es)]
```

Watch the worker log (terminal 2) for the `booted … / evicted …` cycle — in `repair` it's
the same `sandbox-<id>`; in `replace` each boot is a new one.
```
