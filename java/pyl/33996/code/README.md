# PYL-33996 — Kotlin 2.2.0 → 2.4.0 workflow replay non-determinism

Reproduces a Temporal replay `NonDeterministicException` caused **purely by upgrading the Kotlin
compiler** (2.2.0 → 2.4.0). The workflow source is byte-for-byte identical between the two builds; only
`kotlin.version` differs.

## The failure

The parent workflow mirrors `GroupTaskWorkflow` / `GroupTaskHandler`:

- The **main workflow thread** creates children in a loop. Each child is started with a **method
  reference** `Async.function(child::emphasize, …)` and the loop then blocks on
  `Workflow.getWorkflowExecution(child).get()` — one child per workflow task (same shape as
  `GroupTaskHandler.createChildTask` calling `child::startWorkflow`).
- Each child **signals the parent back** (`subTaskCreated`) as it starts, so signals interleave with
  the create loop.
- The **signal handler** runs on a callback `WorkflowThread` and schedules a notification activity via
  `Async.function { activities.composeGreeting(…) }`.

When a single workflow task contains both a child-start (main thread) and a notification (callback
thread), the two commands are produced by different `WorkflowThread`s. Their relative order is decided
by which thread the `DeterministicRunner` advances first, and that turned out to be sensitive to how
the `child::emphasize` method reference is lowered by the Kotlin compiler:

- **Kotlin 2.2.0** lowers the reference to a direct `REF_invokeInterface` method handle → the task
  enqueues `[START_CHILD, ACTIVITY]`.
- **Kotlin 2.4.0** lowers it to a generated static bridge referenced via `REF_invokeStatic` → the task
  enqueues `[ACTIVITY, START_CHILD]`.

So a history recorded on one compiler fails to replay on the other.

## Layout

- `kotlin-2.2.0/`, `kotlin-2.4.0/` — identical Kotlin sources, different `kotlin.version`.
  - `ParentWorkflow.kt` / `ChildWorkflow.kt` / `Activities.kt` — the reproduction workflow.
  - `Recorder.kt` — records a history JSON with an **in-memory** time-skipping
    `TestWorkflowEnvironment` (no external `temporal server` needed).
  - `Replayer.kt` — replays a history JSON against this build's `GreetingWorkflowImpl`.
- `history-kotlin-2.2.0.json`, `history-kotlin-2.4.0.json` — pre-recorded histories.

## Reproduce (no Temporal server required)

Requires JDK 21 and Maven. Run from the `code/` directory.

```bash

export USE_LAMBDA = true

# Build both, and write each module's runtime classpath to target/cp.txt
for v in 2.2.0 2.4.0; do
  ( cd "kotlin-$v" && mvn -q compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt )
done
CP22="kotlin-2.2.0/target/classes:$(cat kotlin-2.2.0/target/cp.txt)"
CP24="kotlin-2.4.0/target/classes:$(cat kotlin-2.4.0/target/cp.txt)"

# 1. Record a history with each compiler
java -cp "$CP22" io.temporal.poc.sample.Recorder history-kotlin-2.2.0.json
java -cp "$CP24" io.temporal.poc.sample.Recorder history-kotlin-2.4.0.json

# 2. Controls — a history replays cleanly on the compiler that produced it
java -cp "$CP22" io.temporal.poc.sample.Replayer history-kotlin-2.2.0.json   # Replay OK
java -cp "$CP24" io.temporal.poc.sample.Replayer history-kotlin-2.4.0.json   # Replay OK

# 3. Cross-replay — NonDeterministicException both ways
java -cp "$CP24" io.temporal.poc.sample.Replayer history-kotlin-2.2.0.json   # 2.2.0 history on 2.4.0 code
java -cp "$CP22" io.temporal.poc.sample.Replayer history-kotlin-2.4.0.json   # 2.4.0 history on 2.2.0 code
```

(Pre-recorded `history-kotlin-2.2.0.json` / `history-kotlin-2.4.0.json` are checked in, so you can skip
step 1 and go straight to the cross-replay.)

### Expected output

Controls pass:

```
# 2.2.0 history on 2.2.0 code, and 2.4.0 history on 2.4.0 code
Replay OK: no non-determinism for ...
```

Cross-replays fail:

```
# 2.2.0 history on 2.4.0 code
io.temporal.worker.NonDeterministicException: [TMPRL1100] Failure handling event 47 of type
'EVENT_TYPE_START_CHILD_WORKFLOW_EXECUTION_INITIATED' during replay. [TMPRL1100] Event 47 of type
EVENT_TYPE_START_CHILD_WORKFLOW_EXECUTION_INITIATED does not match command type COMMAND_TYPE_SCHEDULE_ACTIVITY_TASK.

# 2.4.0 history on 2.2.0 code
io.temporal.worker.NonDeterministicException: [TMPRL1100] Failure handling event 39 of type
'EVENT_TYPE_ACTIVITY_TASK_SCHEDULED' during replay. [TMPRL1100] Event 39 of type
EVENT_TYPE_ACTIVITY_TASK_SCHEDULED does not match command type COMMAND_TYPE_START_CHILD_WORKFLOW_EXECUTION.
```

## Note on environment

Verified with **`temporal-sdk` 1.33.0**, **JDK 21** (`openjdk 21`, arm64/macOS), Maven 3.9. The exact
command order the `DeterministicRunner` produces depends on the SDK's thread-scheduling, so on a
different SDK/JDK the *direction* of the flip could differ (e.g. which side enqueues `ACTIVITY` first) —
but the histories still mismatch on cross-replay, which is the same bug. If you cannot reproduce, first
confirm the two builds actually emit different orders using the check below.

## Confirming the divergence directly

Every task that contains both commands flips order between the two histories:

```
kotlin 2.2.0:  47:START_CHILD , 48:ACTIVITY     (START_CHILD first)
kotlin 2.4.0:  39:ACTIVITY    , 40:START_CHILD   (ACTIVITY first)
```
