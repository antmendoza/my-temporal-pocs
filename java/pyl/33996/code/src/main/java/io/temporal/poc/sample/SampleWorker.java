package io.temporal.poc.sample;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class SampleWorker {

    static final String TASK_QUEUE = "sample-queue";

    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);

        // parent and child workflows are both registered on the same worker
        worker.registerWorkflowImplementationTypes(
                GreetingWorkflowImpl.class, GreetingChildWorkflowImpl.class);
        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

        factory.start();
        System.out.println("Worker started, polling '" + TASK_QUEUE + "'");
    }
}