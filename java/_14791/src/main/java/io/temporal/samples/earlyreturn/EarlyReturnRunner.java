package io.temporal.samples.earlyreturn;

import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.api.workflowservice.v1.CountWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.GetSystemInfoRequest;
import io.temporal.client.*;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import javax.net.ssl.SSLException;

public class EarlyReturnRunner {
  private static final String TASK_QUEUE = "EarlyReturnTaskQueue";

  static final TemporalProperties properties = new TemporalProperties();
  GrpcLoggingInterceptor grpcLoggingInterceptor = new GrpcLoggingInterceptor();

  public static void main(String[] args) throws SSLException {
    EarlyReturnRunner runner = new EarlyReturnRunner();
    WorkflowClient client = runner.setupWorkflowClient();
    runner.startWorker(client);

    // sleep(3000);

    //    runner.makeGetSystemInfoCall(client);

    //    runner.makeCountWorkflowsCall(client);

    runner.runWorkflowWithUpdateWithStart(client, "first_workflow");
    runner.runWorkflowWithUpdateWithStart(client, "second_workflow");
    runner.runWorkflowWithUpdateWithStart(client, "third_workflow");

    System.exit(0);
  }

  private static void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void makeGetSystemInfoCall(WorkflowClient client) {
    client
        .getWorkflowServiceStubs()
        .blockingStub()
        .getSystemInfo(GetSystemInfoRequest.newBuilder().build());
  }

  private void makeCountWorkflowsCall(WorkflowClient client) {
    client
        .getWorkflowServiceStubs()
        .blockingStub()
        .countWorkflowExecutions(
            CountWorkflowExecutionsRequest.newBuilder()
                .setNamespace(properties.getTemporalNamespace())
                .build());
  }

  public WorkflowClient setupWorkflowClient() throws SSLException {

    WorkflowServiceStubsOptions.Builder builder =
        WorkflowServiceStubsOptions.newBuilder()
            .setTarget(properties.getTemporalTargetEndpoint())
            .setGrpcClientInterceptors(List.of(grpcLoggingInterceptor));

    if (!properties.isTemporalLocalServer()) {

      InputStream clientCert =
          EarlyReturnRunner.class.getResourceAsStream(properties.getTemporalCertLocation());
      InputStream clientKey =
          EarlyReturnRunner.class.getResourceAsStream(properties.getTemporalKeyLocation());
      builder.setSslContext(SimpleSslContextBuilder.forPKCS8(clientCert, clientKey).build());
    }

    WorkflowServiceStubs service =
        WorkflowServiceStubs.newConnectedServiceStubs(builder.build(), Duration.ofSeconds(1));
    return WorkflowClient.newInstance(
        service,
        WorkflowClientOptions.newBuilder().setNamespace(properties.getTemporalNamespace()).build());
  }

  private void startWorker(WorkflowClient client) {
    WorkerFactory factory = WorkerFactory.newInstance(client);
    Worker worker = factory.newWorker(TASK_QUEUE);

    worker.registerWorkflowImplementationTypes(TransactionWorkflowImpl.class);
    worker.registerActivitiesImplementations(new TransactionActivitiesImpl());

    factory.start();
    System.out.println("Worker started");
  }

  private void runWorkflowWithUpdateWithStart(WorkflowClient client, String description) {

    System.out.println("\n Running " + description);

    grpcLoggingInterceptor.resetFirstWorkflowTaskRecords();

    TransactionRequest txRequest =
        new TransactionRequest(
            "Bob", "Alice",
            1000); // Change this amount to a negative number to have initTransaction fail

    WorkflowOptions options = buildWorkflowOptions(description);
    TransactionWorkflow workflow = client.newWorkflowStub(TransactionWorkflow.class, options);

    try {
      WorkflowClient.executeUpdateWithStart(
          workflow::returnInitResult,
          UpdateOptions.<TxResult>newBuilder().build(),
          new WithStartWorkflowOperation<>(workflow::processTransaction, txRequest));

      WorkflowStub.fromTyped(workflow).getResult(TxResult.class);

      long latencyMillis =
          new InspectWorkflowHistory(client, options.getWorkflowId())
              .getFirstWorkflowTaskLatencyMillis();

      System.out.println(" >  first workflow task latency");
      System.out.println("   > " + latencyMillis + " ms : ->  WorkflowHistory Latency");
      System.out.println(
          "   > "
              + grpcLoggingInterceptor.getTimeFirstWorkflowTaskExecution().toEpochMilli()
              + " ms : ->  Latency measured in GrpcInterceptor (from PollWorkflowTaskResponse to RespondWorkflowTaskCompleted)");

    } catch (Exception e) {
      System.err.println("Transaction initialization failed: " + e.getMessage());
    }
  }

  // Build WorkflowOptions with task queue and unique ID
  private static WorkflowOptions buildWorkflowOptions(String description) {
    return WorkflowOptions.newBuilder()
        .setTaskQueue(TASK_QUEUE)
        .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_FAIL)
        .setWorkflowId(description)
        .build();
  }
}
