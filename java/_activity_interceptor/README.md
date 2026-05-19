# Audit Logging POC

Every main/marked for tracking activity should be followed by an `auditLogging` step. 
This should be implemented as a separate activity, but for cost efficiency reasons it is implemented as part of the same activity executions (resulting each activity in two operations: one for the activity itself, one for the audit logging)

The audit is attempted *inline on the activity worker first*, and only if that inline attempt fails (and the caller asked for tracking) does the workflow schedule a real `auditLogging` Temporal activity as a fallback.

## Components

### `SimpleActivityInterceptor` (activity-side)

Wraps `ActivityInboundCallsInterceptor.execute(...)`. After each activity attempt:

- If the activity returned a `MyActivityResult`, invokes `new GreetingActivitiesImpl().auditLogging()` as a **direct POJO call** (bypasses the activity stub — no scheduled Temporal activity, no history event, no retries; runs in the worker thread of the activity currently being intercepted).
- If that inline call succeeds, returns a `MyActivityResult` with `auditLoggingActivitySuccess = true`.
- If it errors, catches the exception and returns a `MyActivityResult` with `auditLoggingActivitySuccess = false`.

The `auditLoggingActivitySuccess` flag is what the workflow side reads to decide whether to schedule the fallback.

### `SimpleWorkflowInterceptor` (workflow-side)

- **`WorkflowOutboundCallsInterceptor.executeActivity`**: blocks on the activity result and, if `isAuditLoggingActivitySuccess() == false`, schedules a fallback `auditLogging` activity via `Async.procedure(...)` and adds its `Promise<Void>` to `auditLoggingPromises`.
- **`WorkflowInboundCallsInterceptor.execute`**: runs the workflow, then awaits `Promise.allOf(outbound.auditLoggingPromises)` so the workflow only completes once every fallback audit activity has finished.


Both interceptors are registered via `WorkerFactoryOptions.setWorkerInterceptors(...)` in `Starter`.

## Run

Start a local Temporal server on `:7233`, then:

```bash
./mvnw compile exec:java -Dexec.mainClass="io.temporal.samples.Starter"
```

The workflow runs `activity_1` (`trackAuditLogging=true`), `activity_2` (`trackAuditLogging=false`), `activity_3` (`trackAuditLogging=true`). `GreetingActivitiesImpl.auditLogging()` throws for the first 9 invocations and only succeeds on the 10th, so the inline audit fails initially and the workflow ends up scheduling fallback `auditLogging` activities for `activity_1` and `activity_3` (not `activity_2`, since its input opts out).
