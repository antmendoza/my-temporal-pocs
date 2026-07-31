# Method references spawn a WorkflowThread per call

`Async.function` / `Async.procedure` with a **Kotlin method reference** to a stub spawns one
`WorkflowThread` per call instead of running it inline as it does in Java. A wide fan-out then holds
one workflow-pool thread per in-flight activity.

## How to run

Requires JDK 21, Maven, and a local server (`temporal server start-dev`; override the address with
`TEMPORAL_ADDRESS`). From this `code/` directory:


## Reproduction


Each run fans out 100 activities twice — once via a method reference (PATH 1) and once via a lambda
(PATH 2) — and prints the peak `temporal_workflow_active_thread_count`.



### Kotlin 

- Run [AsyncActivityFanOutComparison.kt](src/main/kotlin/io/temporal/samples/asyncactivityfanout/AsyncActivityFanOutComparison.kt)

```
=== Async fan-out of 100 activities ===
PATH 1 (stub method reference)  temporal_workflow_active_thread_count peak: 101
PATH 2 (lambda wrapper)         temporal_workflow_active_thread_count peak: 101
```

PATH 1 peaks at 101 — one thread per call

### Java 

- Run [AsyncActivityFanOutComparisonJava.java](src/main/java/io/temporal/samples/asyncactivityfanout/AsyncActivityFanOutComparisonJava.java)

The same behavior is not observed in Java:

```
=== Async fan-out of 100 activities (Java) ===
PATH 1 (stub method reference)  temporal_workflow_active_thread_count peak: 1
PATH 2 (lambda wrapper)         temporal_workflow_active_thread_count peak: 101
```

PATH 1 peaks at 1 — the method reference is inlined, as expected.

## Environment

temporal-sdk 1.33.0, temporal-kotlin on the classpath, Kotlin 2.4.0, JDK 21.
