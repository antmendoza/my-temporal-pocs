package io.temporal.samples.hello;

import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.api.taskqueue.v1.TaskQueue;
import io.temporal.api.workflowservice.v1.*;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.VersioningBehavior;
import io.temporal.common.WorkerDeploymentVersion;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerDeploymentOptions;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.Workflow;

import java.util.concurrent.CompletableFuture;

public class Example_1 {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue"+System.currentTimeMillis();

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";

    static final String namespace = "default";

    static final String deploymentName = "loan-proces"+System.currentTimeMillis();

    static final String deploymentVersion = "v1";


    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs
                .newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .build());

        WorkflowClient client = WorkflowClient.newInstance(service);


        listExecutions(client);

        startWorkflow(client);

        {
            WorkerFactory factory = WorkerFactory.newInstance(client);
            WorkerOptions builder = WorkerOptions.newBuilder()
                    .build();

            Worker worker = factory.newWorker(TASK_QUEUE, builder);

            worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

            worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
            factory.start();

        }
        System.out.println("\n********Started unversioned worker ***********\n");


        // Sleep to allow workflow to iterate a few times
        sleep(5_000);


        {
            WorkerFactory factory = WorkerFactory.newInstance(client);
            WorkerOptions builder = WorkerOptions.newBuilder()
                    .setDeploymentOptions(
                            WorkerDeploymentOptions.newBuilder()
                                    .setUseVersioning(true)
                                    .setVersion(WorkerDeploymentVersion.fromCanonicalString(deploymentName + "." + deploymentVersion))
                                    .setDefaultVersioningBehavior(VersioningBehavior.AUTO_UPGRADE)
                                    .build())
                    .build();

            Worker worker = factory.newWorker(TASK_QUEUE, builder);

            worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

            worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
            factory.start();

        }

        System.out.println("\n********Worker started with version " + deploymentName + ":" + deploymentVersion + " ***********\n");

        // Sleep to allow workflow to iterate a few times
        sleep(5_000);


        setCurrentVersionLoanV1(client);


        System.out.println("\n********Worker deployment version set to " + deploymentName + ":" + deploymentVersion + " as current***********\n");


    }

    private static void setCurrentVersionLoanV1(WorkflowClient client) {
        client.getWorkflowServiceStubs().blockingStub()
                .setWorkerDeploymentCurrentVersion(SetWorkerDeploymentCurrentVersionRequest.newBuilder()
                        .setNamespace(namespace)
                        .setDeploymentName(deploymentName)
                        .setBuildId(deploymentVersion).build());
    }

    private static void listExecutions(WorkflowClient client) {


        CompletableFuture.runAsync(() -> {
            while (true) {

                try {


                    sleep(3_000);

                    System.out.println("Deployment info for task queue " + TASK_QUEUE + ", deployment version " +deploymentName +":");

                    ListWorkflowExecutionsResponse listWorkflowExecutionsResponse = client.getWorkflowServiceStubs().blockingStub().listWorkflowExecutions(ListWorkflowExecutionsRequest.newBuilder()
                            .setNamespace(namespace)
                            .setQuery("`TemporalWorkerDeploymentVersion`=\"" + deploymentName + ":"+deploymentVersion+"\" AND `ExecutionStatus`=\"Running\"")
                            .build());


                    System.out.println("- Num running executions in worker deployment version " + listWorkflowExecutionsResponse.getExecutionsCount());

                    listWorkflowExecutionsResponse.getExecutionsList().forEach(v -> {
 //                       System.out.println("    - Found execution with " + deploymentName + ":"+deploymentVersion+" " + v.getExecution().getWorkflowId());
                    });

                    DescribeTaskQueueResponse taskqueue = client.getWorkflowServiceStubs().blockingStub().describeTaskQueue(
                            DescribeTaskQueueRequest.newBuilder()
                            .setReportStats(true)
                                    .setNamespace(namespace)
                            .setTaskQueue(TaskQueue.newBuilder().setName(TASK_QUEUE).build()).build());

                    System.out.println("- TaskQueue Versions info: \n" +taskqueue.getVersioningInfo().getCurrentDeploymentVersion());


                } catch (Exception e) {

                        e.printStackTrace();
                }

            }
        });


    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void startWorkflow(WorkflowClient client) {

        System.out.println("\n*********Starting Workflow***********\n");

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
    }

}
