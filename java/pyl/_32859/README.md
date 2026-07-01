# _32859

Cross-version replay check for the Temporal Java SDK.

This repository demonstrates how gzip compression (enabled by default in java v1.36.0) can cause NDE.

`run.sh` builds two self-contained jars, each pinned to a different SDK version, then:

1. **WorkerMain** (`WORKER_VER`) runs the workflow to completion and exits.
2. **QueryClient** (`QUERY_VER`) starts its own worker and queries the workflow, so the
   query is answered by replaying the history on `QUERY_VER` — exercising cross-version replay.

## Version overrides

Defaults are `WORKER_VER=1.35.0` and `QUERY_VER=1.36.0`. Override either:

```bash
WORKER_VER=1.34.0 ./run.sh     # change the workflow-run SDK version
QUERY_VER=1.35.0 ./run.sh      # change the query/replay SDK version
```


## Run
The code connects to Temporal Cloud, namespace and other properties are read from [temporal.properties](src/main/resources/temporal.properties)

The API_KEY is read from the environment variable `TEMPORAL_API_KEY`.

``
export TEMPORAL_API_KEY= your-temporal-api-key
``

Then run the script:

```bash
./run.sh
```

This builds both jars, runs the workflow on `WORKER_VER`, then queries/replays it on `QUERY_VER`.


Watch the `Running workflow with SDK version: ...` and `Replaying workflow with SDK version: ...`
lines to confirm which version handled each phase.


## Output

During replay the workflow throws a `io.temporal.worker.NonDeterministicException: [TMPRL1100]`

```
>> building workflow-run jar (SDK 1.35.0) -> target/app-1.35.0.jar
>> building query/replay jar (SDK 1.36.0) -> target/app-1.36.0.jar
>> running workflow to completion on SDK 1.35.0
WorkerMain SDK version: 1.35.0
Connecting to Temporal Cloud: us-west-2.aws.api.temporal.io:7233 (namespace antonio.a2dd6)
wokflow log: Running workflow with SDK version: 1.35.0
Workflow finished; shutting worker down.
-----------

>> querying (starts its own worker) on SDK 1.36.0
QueryClient SDK version: 1.36.0
Connecting to Temporal Cloud: us-west-2.aws.api.temporal.io:7233 (namespace antonio.a2dd6)
Querying workflowId=_32859-workflow
wokflow log: Replaying workflow with SDK version: 1.36.0
Exception in thread "main" io.temporal.client.WorkflowQueryException: workflowId='_32859-workflow', runId=''
        at io.temporal.client.WorkflowStubImpl.throwAsWorkflowFailureExceptionForQuery(WorkflowStubImpl.java:562)
        at io.temporal.client.WorkflowStubImpl.query(WorkflowStubImpl.java:330)
        at io.temporal.client.WorkflowStubImpl.query(WorkflowStubImpl.java:316)
        at io.temporal.samples.hello.QueryClient.main(QueryClient.java:35)
Caused by: io.grpc.StatusRuntimeException: INVALID_ARGUMENT: io.temporal.internal.statemachines.InternalWorkflowTaskException: Failure handling event 6 of type 'EVENT_TYPE_ACTIVITY_TASK_SCHEDULED' during replay. {WorkflowTaskStartedEventId=0, CurrentStartedEventId=3}
        at io.temporal.internal.statemachines.WorkflowStateMachines.createEventProcessingException(WorkflowStateMachines.java:463)
        at io.temporal.internal.statemachines.WorkflowStateMachines.handleEventsBatch(WorkflowStateMachines.java:364)
        at io.temporal.internal.statemachines.WorkflowStateMachines.handleEvent(WorkflowStateMachines.java:323)
        at io.temporal.internal.replay.ReplayWorkflowRunTaskHandler.applyServerHistory(ReplayWorkflowRunTaskHandler.java:261)
        at io.temporal.internal.replay.ReplayWorkflowRunTaskHandler.handleWorkflowTaskImpl(ReplayWorkflowRunTaskHandler.java:243)
        at io.temporal.internal.replay.ReplayWorkflowRunTaskHandler.handleDirectQueryWorkflowTask(ReplayWorkflowRunTaskHandler.java:216)
        at io.temporal.internal.replay.ReplayWorkflowTaskHandler.handleWorkflowTaskWithQuery(ReplayWorkflowTaskHandler.java:109)
        at io.temporal.internal.replay.ReplayWorkflowTaskHandler.handleWorkflowTask(ReplayWorkflowTaskHandler.java:80)
        at io.temporal.internal.worker.WorkflowWorker$TaskHandlerImpl.handleTask(WorkflowWorker.java:615)
        at io.temporal.internal.worker.WorkflowWorker$TaskHandlerImpl.handle(WorkflowWorker.java:438)
        at io.temporal.internal.worker.WorkflowWorker$TaskHandlerImpl.handle(WorkflowWorker.java:376)
        at io.temporal.internal.worker.PollTaskExecutor.lambda$process$1(PollTaskExecutor.java:80)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
        at java.base/java.lang.Thread.run(Thread.java:1516)
Caused by: java.lang.RuntimeException: Version: failure executing RESULT_NOTIFIED_REPLAYING->NON_MATCHING_EVENT, transition history is [CREATED->CHECK_EXECUTION_STATE, REPLAYING->SCHEDULE, MARKER_COMMAND_CREATED_REPLAYING->RECORD_MARKER]
        at io.temporal.internal.statemachines.StateMachine.executeTransition(StateMachine.java:143)
        at io.temporal.internal.statemachines.StateMachine.handleExplicitEvent(StateMachine.java:73)
        at io.temporal.internal.statemachines.EntityStateMachineBase.explicitEvent(EntityStateMachineBase.java:75)
        at io.temporal.internal.statemachines.VersionStateMachine$InvocationStateMachine.handleEvent(VersionStateMachine.java:168)
        at io.temporal.internal.statemachines.CancellableCommand.handleEvent(CancellableCommand.java:53)
        at io.temporal.internal.statemachines.WorkflowStateMachines.handleCommandEvent(WorkflowStateMachines.java:601)
        at io.temporal.internal.statemachines.WorkflowStateMachines.handleSingleEvent(WorkflowStateMachines.java:495)
        at io.temporal.internal.statemachines.WorkflowStateMachines.handleEventsBatch(WorkflowStateMachines.java:362)
        ... 13 more
Caused by: io.temporal.worker.NonDeterministicException: [TMPRL1100] getVersion call before the existing version marker event. The most probable cause is retroactive addition of a getVersion call with an existing 'changeId'
        at io.temporal.internal.statemachines.VersionStateMachine$InvocationStateMachine.missingMarkerReplaying(VersionStateMachine.java:344)
        at io.temporal.internal.statemachines.FixedTransitionAction.apply(FixedTransitionAction.java:26)
        at io.temporal.internal.statemachines.StateMachine.executeTransition(StateMachine.java:139)
        ... 20 more

```


#### Disabling GZIP compression


For more details check the code in [TemporalClientFactory.java](src/main/java/io/temporal/samples/hello/TemporalClientFactory.java) method `disableGzipIfSupported`

```bash
export DISABLE_GZIP=false
./run.sh
```

```
>> building workflow-run jar (SDK 1.35.0) -> target/app-1.35.0.jar
>> building query/replay jar (SDK 1.36.0) -> target/app-1.36.0.jar
>> running workflow to completion on SDK 1.35.0
WorkerMain SDK version: 1.35.0
Connecting to Temporal Cloud: us-west-2.aws.api.temporal.io:7233 (namespace antonio.a2dd6)
gRPC compression not supported by this SDK version; skipping
wokflow log: Running workflow with SDK version: 1.35.0
Workflow finished; shutting worker down.
-----------

>> querying (starts its own worker) on SDK 1.36.0
QueryClient SDK version: 1.36.0
Connecting to Temporal Cloud: us-west-2.aws.api.temporal.io:7233 (namespace antonio.a2dd6)
gRPC gzip compression enabled
Querying workflowId=_32859-workflow
wokflow log: Replaying workflow with SDK version: 1.36.0
Query result: something

-----------


```

