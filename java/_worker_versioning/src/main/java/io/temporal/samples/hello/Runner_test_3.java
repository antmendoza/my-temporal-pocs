

package io.temporal.samples.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.common.VersioningBehavior;
import io.temporal.common.WorkerDeploymentVersion;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerDeploymentOptions;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;

public class Runner_test_3 {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";


    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);


        WorkerOptions builder = WorkerOptions.newBuilder().setDeploymentOptions(
                        WorkerDeploymentOptions.newBuilder()
                                .setUseVersioning(true)
                                .setVersion(WorkerDeploymentVersion.fromCanonicalString("test.3"))
                                .setDefaultVersioningBehavior(VersioningBehavior.AUTO_UPGRADE)
                                .build())
                .build();

        Worker worker = factory.newWorker(TASK_QUEUE, builder);


        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

        factory.start();


    }

    
}
