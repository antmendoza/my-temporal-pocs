# First workflow task execution latency 

- Open `temporal.properties to set the configuration

- Run the following command to start the worker and run the sample:

```bash

./mvnw compile exec:java -Dexec.mainClass="io.temporal.samples.earlyreturn.EarlyReturnRunner" 

```

The code runs the workflow [TransactionWorkflowImpl.java](src/main/java/io/temporal/samples/earlyreturn/TransactionWorkflowImpl.java) several times, 
and measures the time it takes to complete the first workflow task for each workflow execution.

There are two measurements:
- Inspecting the Workflow History, which includes network latency
  - [InspectWorkflowHistory.java](src/main/java/io/temporal/samples/earlyreturn/InspectWorkflowHistory.java)

- GRPC Interceptor: from when the worker polls the task (PollWorkflowTaskQueueResponse) to when the worker responds with RespondWorkflowTaskCompleted.
This does not include network latency and other computation time after the interceptor.
  - See [GrpcLoggingInterceptor.java](src/main/java/io/temporal/samples/earlyreturn/GrpcLoggingInterceptor.java)
  