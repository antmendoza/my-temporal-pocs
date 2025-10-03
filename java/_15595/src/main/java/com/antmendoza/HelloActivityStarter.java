package com.antmendoza;

import static com.antmendoza.HelloActivity.TASK_QUEUE;
import static com.antmendoza.HelloActivity.WORKFLOW_ID;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

/** Sample Temporal Workflow Definition that executes a single Activity. */
public class HelloActivityStarter {

  /**
   * With our Workflow and Activities defined, we can now start execution. The main method starts
   * the worker and then the workflow.
   */
  public static void main(String[] args) {

    // Get a Workflow service stub.
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

    /*
     * Get a Workflow service client which can be used to start, Signal, and Query Workflow Executions.
     */
    WorkflowClient client = WorkflowClient.newInstance(service);

    // Create the workflow client stub. It is used to start our workflow execution.
    HelloActivity.GreetingWorkflow workflow =
        client.newWorkflowStub(
            HelloActivity.GreetingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .build());

    String greeting = workflow.getGreeting("World");

    // Display workflow execution results
    System.out.println(greeting);
    System.exit(0);
  }
}
