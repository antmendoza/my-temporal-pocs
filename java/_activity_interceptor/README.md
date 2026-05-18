# Activity & Workflow Interceptors — Audit Logging POC

Demonstrates Temporal Java SDK (1.30.1) worker, workflow, and activity interceptors using an "audit logging" use case: every main activity should be followed by an `auditLogging` step; the audit is attempted *inline on the activity worker first*, and only if that inline attempt fails (and the caller asked for tracking) does the workflow schedule a real `auditLogging` Temporal activity as a fallback.

## Components

### `SimpleActivityInterceptor` (activity-side)

Wraps `ActivityInboundCallsInterceptor.execute(...)`. After each activity attempt:

- If the activity returned a `MyActivityResult`, invokes `new GreetingActivitiesImpl().auditLogging()` as a **direct POJO call** (bypasses the activity stub — no scheduled Temporal activity, no history event, no retries; runs in the worker thread of the activity currently being intercepted).
- If that inline call succeeds, returns a `MyActivityResult` with `auditLoggingActivitySuccess = true`.
- If it throws, catches the exception and returns a `MyActivityResult` with `auditLoggingActivitySuccess = false`.

The `auditLoggingActivitySuccess` flag is what the workflow side reads to decide whether to schedule the fallback.

### `SimpleWorkflowInterceptor` (workflow-side)

- **Outbound (`executeActivity`)**: after each main activity returns, checks `input.getArgs()[0] instanceof MyActivityInput`; if so, and the result reports `auditLoggingActivitySuccess == false` AND the input's `trackAuditLogging == true`, schedules an `auditLogging` Temporal activity via `Async.procedure(auditLoggingStub::auditLogging)` and collects the `Promise<Void>` in `auditLoggingPromises`. The stub is a `final` field of the outbound interceptor, created in its constructor (i.e. at workflow init time — see "Stub-creation timing" below).
- **Inbound (`execute`)**: after the workflow main method returns, awaits all collected promises via `Promise.allOf(outbound.auditLoggingPromises).get()` so the workflow only completes once every fallback audit activity has finished.

Both interceptors are registered via `WorkerFactoryOptions.setWorkerInterceptors(...)` in `Starter`.

## Run

Start a local Temporal server on `:7233`, then:

```bash
./mvnw compile exec:java -Dexec.mainClass="com.antmendoza.temporal.Starter"
```

The workflow runs `activity_1` (`trackAuditLogging=true`), `activity_2` (`trackAuditLogging=false`), `activity_3` (`trackAuditLogging=true`). `GreetingActivitiesImpl.auditLogging()` throws for the first 9 invocations and only succeeds on the 10th, so the inline audit fails initially and the workflow ends up scheduling fallback `auditLogging` activities for `activity_1` and `activity_3` (not `activity_2`, since its input opts out).

## Key finding: where to create `Workflow.newActivityStub(...)`

`Workflow.newActivityStub(...)` must be invoked during the workflow **init phase** (e.g. as a field of the outbound interceptor, populated in its constructor — which runs from `WorkflowInboundCallsInterceptor.init(...)`). Creating it later — inside `executeActivity(...)`, or inside the `Async.procedure(...)` lambda — binds the stub to the in-flight outbound call context and serializes the scheduled activity with the current activity flow, even though the call uses `Async.procedure`.

Observed across three histories with otherwise-identical code:

| Stub created in… | Scheduling behavior |
|---|---|
| Outbound interceptor constructor (field) | `auditLogging` and the next `activity_N` scheduled in the **same** WFT (parallel) |
| Inside `executeActivity(...)` (local var) | Next `activity_N` only scheduled **after** `auditLogging` finishes (sequential) |
| Inside the `Async.procedure(...)` lambda | Same as above — sequential |

If you want a per-call dynamic `setSummary(...)` *and* parallel scheduling, pre-create one stub per source activity name at init time and look it up by `input.getActivityName()` in `executeActivity`.
