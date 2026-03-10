# Workflow Task and Activity Execution Latency

This sample measures the latency of the **first workflow task execution** and **activity executions** in a Temporal workflow, using two complementary approaches for each.

## Overview

The runner starts a worker, executes `TransactionWorkflow` three times using `UpdateWithStart`, and for each run prints latency measurements from both the workflow history and in-process interceptors.

### Workflow Task Latency

| Method | What it measures | Includes network latency? |
|---|---|---|
| **Workflow History** | `WorkflowTaskStarted` → `WorkflowTaskCompleted` history events | Yes |
| **gRPC Interceptor** | `PollWorkflowTaskQueueResponse` received → `RespondWorkflowTaskCompleted` sent | No |

**Workflow History** — [`InspectWorkflowHistory.java`](src/main/java/io/temporal/samples/earlyreturn/InspectWorkflowHistory.java)
reads events 3 and 4 from the fetched workflow history and computes the delta.

**gRPC Interceptor** — [`GrpcLoggingInterceptor.java`](src/main/java/io/temporal/samples/earlyreturn/GrpcLoggingInterceptor.java)
intercepts gRPC calls at the client level and timestamps the poll response and task-completed request.

### Activity Execution Latency

| Method | What it measures | Includes network latency? |
|---|---|---|
| **Workflow History** | `ActivityTaskStarted` → `ActivityTaskCompleted` history events | Yes |
| **Activity Interceptor** | `execute()` entry → `execute()` return in the worker process | No |

**Workflow History** — [`InspectWorkflowHistory.java`](src/main/java/io/temporal/samples/earlyreturn/InspectWorkflowHistory.java)
correlates `ActivityTaskScheduled` (for the activity type name), `ActivityTaskStarted`, and `ActivityTaskCompleted` events via their event IDs.

**Activity Interceptor** — [`ActivityLatencyInterceptor.java`](src/main/java/io/temporal/samples/earlyreturn/ActivityLatencyInterceptor.java)
implements `WorkerInterceptorBase` and wraps each activity's `execute()` with `Instant.now()` timestamps, keyed by activity type.

## Prerequisites

- A running Temporal server (local or cloud)

## Configuration

Open [`temporal.properties`](src/main/resources/temporal.properties) and set your connection details:

```properties
# Local server (no TLS)
temporal.localServer=true
temporal.targetEndpoint=localhost:7233
temporal.namespace=default

# For Temporal Cloud, set localServer=false and provide cert/key paths
```

## Run

```bash
./mvnw compile exec:java -Dexec.mainClass="io.temporal.samples.earlyreturn.EarlyReturnRunner"
```

## Sample Output

```
 Running first_workflow
 >  first workflow task latency
   > 12 ms : ->  WorkflowHistory Latency
   > 9 ms : ->  GrpcInterceptor Latency (from PollWorkflowTaskResponse to RespondWorkflowTaskCompleted)
 >  activity execution latency (ActivityTaskStarted -> ActivityTaskCompleted)
   [MintTransactionId]
     > 105 ms : ->  WorkflowHistory Latency
     > 102 ms : ->  ActivityInterceptor Latency
   [InitTransaction]
     > 108 ms : ->  WorkflowHistory Latency
     > 101 ms : ->  ActivityInterceptor Latency
```

The history latency is typically slightly higher than the interceptor latency because it also includes the network round-trip for the `RespondActivityTaskCompleted` gRPC call.

## Key Files

| File | Description |
|---|---|
| [`EarlyReturnRunner.java`](src/main/java/io/temporal/samples/earlyreturn/EarlyReturnRunner.java) | Entry point — starts worker, runs 3 workflows, prints latency |
| [`TransactionWorkflowImpl.java`](src/main/java/io/temporal/samples/earlyreturn/TransactionWorkflowImpl.java) | Workflow implementation |
| [`InspectWorkflowHistory.java`](src/main/java/io/temporal/samples/earlyreturn/InspectWorkflowHistory.java) | History-based latency measurement (workflow task + activities) |
| [`GrpcLoggingInterceptor.java`](src/main/java/io/temporal/samples/earlyreturn/GrpcLoggingInterceptor.java) | gRPC interceptor for workflow task latency |
| [`ActivityLatencyInterceptor.java`](src/main/java/io/temporal/samples/earlyreturn/ActivityLatencyInterceptor.java) | Worker interceptor for activity execution latency |