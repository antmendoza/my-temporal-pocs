

package io.temporal.samples.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.VersioningOverride;
import io.temporal.common.WorkerDeploymentVersion;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class Client_pinned {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";


    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);


        for (int i = 0; i < 1; i++) {

            // Create the workflow client stub. It is used to start our workflow execution.
            GreetingWorkflow workflow =
                    client.newWorkflowStub(
                            GreetingWorkflow.class,
                            WorkflowOptions.newBuilder()
                                    //.setWorkflowId(WORKFLOW_ID)
                                    .setTaskQueue(TASK_QUEUE)
                                    .setVersioningOverride(new VersioningOverride.PinnedVersioningOverride(
                                            WorkerDeploymentVersion.fromCanonicalString("test.1.4")))
                                    //.setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING)
                                    .build());


            WorkflowClient.start(workflow::mainMethod, "");


            sleepMs(1000);


        }

        // wait workflow execution results
        //workflowStub.getResult(String.class);


        System.exit(0);
    }

    private static void sleepMs(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
