package io.temporal.samples.tracing;

import static io.temporal.samples.tracing.Mode.mode;

import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.client.*;
import io.temporal.opentracing.OpenTracingClientInterceptor;
import io.temporal.samples.tracing.workflow.TracingWorkflow;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class Start_plus_Update {
  public static final WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
  public static final String TASK_QUEUE_NAME = "tracingTaskQueue";

  public static void main(String[] args) {
    String type = mode;

    // Set the OpenTracing client interceptor
    WorkflowClientOptions clientOptions =
        WorkflowClientOptions.newBuilder()
            .setInterceptors(new OpenTracingClientInterceptor(JaegerUtils.getJaegerOptions(type)))
            .build();
    WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);

    WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder()
            .setWorkflowId("tracingWorkflow")
            .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_FAIL)
            .setTaskQueue(TASK_QUEUE_NAME)
            .build();

    // Create typed workflow stub
    TracingWorkflow workflow = client.newWorkflowStub(TracingWorkflow.class, workflowOptions);

    WorkflowStub.fromTyped(workflow).start("updateId");
    WorkflowStub.fromTyped(workflow)
        .startUpdate(
            UpdateOptions.<String>newBuilder()
                .setUpdateName("updateMethod")
                .setWaitForStage(WorkflowUpdateStage.COMPLETED)
                .setResultClass(String.class)
                .build(),
            "test");

    String greeting = WorkflowStub.fromTyped(workflow).getResult(String.class);

    System.out.println("Greeting: " + greeting);

    System.exit(0);
  }
}
