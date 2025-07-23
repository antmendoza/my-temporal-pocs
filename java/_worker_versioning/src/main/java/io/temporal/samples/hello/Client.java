

package io.temporal.samples.hello;

import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.client.WorkflowUpdateStage;
import io.temporal.common.VersioningOverride;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class Client {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";


    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);


        // Create the workflow client stub. It is used to start our workflow execution.
        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING)
                                .build());


        WorkflowClient.start(workflow::mainMethod, "");


        sleepMs(1000);

        final WorkflowStub workflowStub = WorkflowStub.fromTyped(workflow);
        workflowStub
                .startUpdate("getNotification",
                        WorkflowUpdateStage.ACCEPTED,
                        String.class, "1");

        workflowStub
                .startUpdate("getNotification",
                        WorkflowUpdateStage.ACCEPTED,
                        String.class, "2");

        sleepMs(1000);


        workflowStub
                .signal("aggregateNotification",
                        Void.class);

        // wait workflow execution results
        workflowStub.getResult(String.class);


       // System.exit(0);
    }

    private static void sleepMs(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
