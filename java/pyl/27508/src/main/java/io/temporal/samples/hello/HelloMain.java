package io.temporal.samples.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import io.temporal.worker.tuning.PollerBehaviorAutoscaling;


public class HelloMain {

    static final String TASK_QUEUE = "HelloActivityPluginTaskQueue";


    public static void main(String[] args) {

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client =
                WorkflowClient.newInstance(
                        service);

        WorkerFactory factory = WorkerFactory.newInstance(client);
        WorkerOptions.Builder builder = WorkerOptions.newBuilder();
        builder.setWorkflowTaskPollersBehavior(new PollerBehaviorAutoscaling());

        var validated = builder.validateAndBuildWithDefaults();

        System.out.println("getMaxConcurrentWorkflowTaskPollers: " + validated.getMaxConcurrentWorkflowTaskPollers());
        System.out.println("getWorkflowTaskPollersBehavior: " + validated.getWorkflowTaskPollersBehavior());

        factory.newWorker(TASK_QUEUE, validated);


    }

}
