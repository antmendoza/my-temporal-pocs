package io.temporal.samples;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.TerminateWorkflowExecutionRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.config.Client;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.ArrayList;
import java.util.List;

public class HelloClient {

    private static final String SCHEDULE_ID = "HelloSchedule";

    public static void main(String[] args) throws InterruptedException {

        final Client client = new Client();

        final List<String> workflows = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            String workflowId = "WORKFLOW_ID" + "_" + i;


            WorkflowOptions workflowOptions =
                    WorkflowOptions.newBuilder()
                            .setTaskQueue("TASK_QUEUE")
                            .setWorkflowId(workflowId).build();

            GreetingWorkflow workflow =
                    client.getWorkflowClient()
                            .newWorkflowStub(GreetingWorkflow.class,
                            workflowOptions);

            WorkflowClient.start(workflow::greet, "World");

            workflows.add(workflowId);

            Thread.sleep(2000);

        }


        for (String workflowId : workflows) {

            client.getWorkflowClient().getWorkflowServiceStubs().blockingStub()
                    .terminateWorkflowExecution(TerminateWorkflowExecutionRequest.newBuilder()
                            .setNamespace(client.getTemporalNamespace())
                            .setReason("whatever")
                            .setWorkflowExecution(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build())
                            .build());


            Thread.sleep(30_000);

        }


    }

    @WorkflowInterface
    public static interface GreetingWorkflow {

        @WorkflowMethod
        void greet(String name);

    }


}
