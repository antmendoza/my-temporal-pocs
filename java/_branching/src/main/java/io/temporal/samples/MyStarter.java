package io.temporal.samples;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.util.List;

public class MyStarter {

    static final String TASK_QUEUE = "HelloActivityWithChildTaskQueue";

    static final String WORKFLOW_ID = "HelloActivityWithChildWorkflow";

    public static void main(String[] args) {

        // Get a Workflow service stub.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);

        // Create the workflow client stub. It is used to start our workflow execution.
        String workflowId = WORKFLOW_ID + Math.random();
        Workflow_.ParentWorkflow workflow =
                client.newWorkflowStub(
                        Workflow_.ParentWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(workflowId)
                                .setTaskQueue(TASK_QUEUE)
                                .build());


        WorkflowClient.start(workflow::start, new WorkflowInput());

        client.newUntypedWorkflowStub(workflowId).signal("processSignal", "job1");
        client.newUntypedWorkflowStub(workflowId).signal("processSignal", "job2");

        // Display workflow execution results
        System.out.println(client.newUntypedWorkflowStub(workflowId).getResult(List.class));
        System.exit(0);
    }
}
