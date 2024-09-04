package com.antmendoza;

import static com.antmendoza.MyStarter.TASK_QUEUE;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.*;

public class MyWorker {

  public static void main(String[] args) {

    // Get a Workflow service stub.
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

    /*
     * Get a Workflow service client which can be used to start, Signal, and Query Workflow Executions.
     */
    WorkflowClient client = WorkflowClient.newInstance(service);

    WorkerFactory factory =
        WorkerFactory.newInstance(client, WorkerFactoryOptions.newBuilder().build());

    Worker worker = factory.newWorker(TASK_QUEUE, WorkerOptions.newBuilder().build());

    worker.registerWorkflowImplementationTypes(
        WorkflowImplementationOptions.newBuilder().build(), Workflow_.ParentWorkflowImpl.class);

    worker.registerActivitiesImplementations(new Workflow_.MyActivitiesImpl());

    factory.start();
  }
}
