package com.antmendoza;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.*;

public class MyStarter {

  static final String TASK_QUEUE = "HelloActivityWithChildTaskQueue";

  static final String WORKFLOW_ID = "HelloActivityWithChildWorkflow";

  public static void main(String[] args) {

    // Get a Workflow service stub.
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

    WorkflowClient client = WorkflowClient.newInstance(service);

    // Create the workflow client stub. It is used to start our workflow execution.
    Workflow_.ParentWorkflow workflow =
        client.newWorkflowStub(
            Workflow_.ParentWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID + Math.random())
                .setTaskQueue(TASK_QUEUE)
                .build());

    String greeting = workflow.start(new WorkflowInput());

    // Display workflow execution results
    System.out.println(greeting);
    System.exit(0);
  }
}
