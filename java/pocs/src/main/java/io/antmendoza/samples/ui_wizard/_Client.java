package io.antmendoza.samples.ui_wizard;

import io.antmendoza.samples.WorkflowClientFactory;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;

public class _Client {

    public static String TASK_QUEUE = "my-task-queue";


    public static void main(String[] args) {


        WorkflowClient client = WorkflowClientFactory.get();
        final WorkflowOptions build = WorkflowOptions
                .newBuilder()
                .setTaskQueue(TASK_QUEUE)
                .build();


        WizardUIWorkflow workflow = client
                .newWorkflowStub(WizardUIWorkflow.class,
                        build);
        WorkflowExecution execution = WorkflowClient.start(workflow::run);

        submitScreen(workflow, 1);
        submitScreen(workflow, 2);
        submitScreen(workflow, 3);


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