# First workflow task execution latency 

- Open `temporal.properties to set the configuration

- Run the following command to start the worker and run the sample:

```bash

./mvnw compile exec:java -Dexec.mainClass="io.temporal.samples.earlyreturn.EarlyReturnRunner" 

```

The code run the workflow [TransactionWorkflowImpl.java](src/main/java/io/temporal/samples/earlyreturn/TransactionWorkflowImpl.java) several times, 
and measures the time it takes to complete the first workflow task for each workflow execution.

There are two measurements:
- From Workflow history, which includes network latency
  - [InspectWorkflowHistory.java](src/main/java/io/temporal/samples/earlyreturn/InspectWorkflowHistory.java)

- Interceptors from when the worker polls the task (PollWorkflowTaskQueueResponse) to when the worker responds with RespondWorkflowTaskCompleted.
this does not include network latency and other computation time after the interceptor.
  - See [GrpcLoggingInterceptor.java](src/main/java/io/temporal/samples/earlyreturn/GrpcLoggingInterceptor.java)
  