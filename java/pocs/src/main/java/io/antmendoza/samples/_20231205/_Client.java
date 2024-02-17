package io.antmendoza.samples._20231205;

import io.antmendoza.samples.WorkflowClientFactory;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;

import java.util.concurrent.CompletableFuture;

public class _Client {

    public static String TASK_QUEUE = "my-task-queue";


    public static void main(String[] args) {


        for (int i = 0; i < 1000; i++) {


            WorkflowClient client = WorkflowClientFactory.get();
            final WorkflowOptions build = WorkflowOptions
                    .newBuilder()
                    .setTaskQueue(TASK_QUEUE)
                    .build();


            WizardUIWorkflow workflow = client
                    .newWorkflowStub(WizardUIWorkflow.class,
                            build);
            WorkflowExecution execution = WorkflowClient.start(workflow::run);

            CompletableFuture.runAsync(() -> {
                try {


                    submitScreen(workflow, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(">>>>> " + e.getClass());
                }
            });
        }


    }

    private static void submitScreen(WizardUIWorkflow workflow, int r) {
        try {
            workflow
                    .submitScreen(new UIRequest(r + ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}