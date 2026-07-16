package io.temporal.poc.sample;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.util.UUID;

public class SampleStarter {

    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);




        GreetingWorkflow workflow = client.newWorkflowStub(
                GreetingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(SampleWorker.TASK_QUEUE)
                        .setWorkflowId("sample-" + UUID.randomUUID())
                        .build());

        WorkflowClient.start(workflow::greet, "");



        try {
            Thread.sleep(2_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        workflow.signal("test");





        try {
            Thread.sleep(40_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Result: " );
    }
}