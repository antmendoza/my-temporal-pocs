package io.temporal.samples.hello;

import com.google.protobuf.FieldMask;
import io.temporal.api.batch.v1.BatchOperationUpdateWorkflowExecutionOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.api.taskqueue.v1.TaskQueue;
import io.temporal.api.workflow.v1.VersioningOverride;
import io.temporal.api.workflow.v1.WorkflowExecutionOptions;
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

import java.util.concurrent.CompletableFuture;


public class Example_8 {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue" + System.currentTimeMillis();

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";

    static final String namespace = "default";

    static final String deploymentName = "loan-proces" + System.currentTimeMillis();

    static final String deploymentVersion_v1 = "v1";
    static final String deploymentVersion_v2 = "v2";


    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs
                .newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .build());

        WorkflowClient client = WorkflowClient.newInstance(service);


//        listExecutions(client);


        for (int i = 0; i < 10; i++) {
            startWorkflow(client, 30, WORKFLOW_ID + i);
        }


        WorkerFactory f1 = startWorkerInVersion(client, deploymentVersion_v1, GreetingWorkflowImpl.class);

        System.out.println("\n********Worker started with version " + deploymentName + ":" + deploymentVersion_v1 + " ***********\n");

        sleep(2_000);

        setCurrentVersionLoan(client, deploymentVersion_v1);



        sleep(2_000);
        //moveAutoupgradeToPinned(client, deploymentVersion_v1);

        if (false) {


            System.out.println("\n********Worker deployment version set to " + deploymentName + ":" + deploymentVersion_v1 + " as current***********\n");


            //give time to the worker to start and process workflows
            sleep(5_000);


            startWorkerInVersion(client, deploymentVersion_v2, GreetingWorkflowImpl_withNDE_8.class);
            sleep(2_000);

            setRampingVersion(client, deploymentVersion_v2, 20);

            //setCurrentVersionLoan(client, deploymentVersion_v2);

            //f1.shutdownNow();


            //workflow start failing with NDE, put v1 back and set as current version
            //startWorkerInVersion(client, deploymentVersion_v1, GreetingWorkflowImpl.class);


            sleep(10_000);

            for (int i = 10; i < 20; i++) {
                //     startWorkflow(client, 30, WORKFLOW_ID + i);
            }

            sleep(10_000);

//        moveAutoupgradeToPinned(client, deploymentVersion_v2);


            //      sleep(5_000);

            setRampingVersion(client, deploymentVersion_v2, 0);


            //deleteVersion(client, deploymentVersion_v2);

        }
    }


    private static void moveAutoupgradeToPinned(WorkflowClient client, String deploymentVersion) {
        client.getWorkflowServiceStubs().blockingStub().startBatchOperation(
                StartBatchOperationRequest.newBuilder()
                        .setNamespace(namespace)
                        .setReason("Upgrade workflows to new deployment version")
                        .setJobId(System.currentTimeMillis() + "")
                        .setVisibilityQuery("TemporalWorkerDeploymentVersion=\"" + deploymentName + ":" + deploymentVersion + "\"")
                        .setUpdateWorkflowOptionsOperation(BatchOperationUpdateWorkflowExecutionOptions.newBuilder()
                                .setWorkflowExecutionOptions(WorkflowExecutionOptions.newBuilder()
                                        .setVersioningOverride(VersioningOverride.newBuilder()
                                                .setPinned(VersioningOverride.PinnedOverride.newBuilder().setVersion(
                                                        io.temporal.api.deployment.v1.WorkerDeploymentVersion.newBuilder().setDeploymentName(deploymentName)
                                                                .setBuildId(deploymentVersion).build()).build()).build())
                                        .build())
                                .setUpdateMask(
                                        FieldMask.newBuilder()
                                                // Path is relative to WorkflowExecutionOptions
                                                .addPaths("versioning_override")
                                                .build())
                                .build())
                        .build()

        );

        System.out.println("\n********Move autoupgrade workflows to pinned for deployment " + deploymentName + " ***********\n");

    }

    private static void setRampingVersion(WorkflowClient client, String deploymentVersion, int rampingPercentage) {

        //set ramping version
        client.getWorkflowServiceStubs().blockingStub()
                .setWorkerDeploymentRampingVersion(SetWorkerDeploymentRampingVersionRequest.newBuilder()
                        .setNamespace(namespace)
                        .setDeploymentName(deploymentName)
                        .setPercentage(rampingPercentage)
                        .setBuildId(deploymentVersion).build());
    }

    private static void deleteVersion(WorkflowClient client, String deploymentVersion) {


        client.getWorkflowServiceStubs().blockingStub()
                .deleteWorkerDeploymentVersion(DeleteWorkerDeploymentVersionRequest.newBuilder()
                        .setNamespace(namespace)
                        .setDeploymentVersion(io.temporal.api.deployment.v1.WorkerDeploymentVersion.newBuilder().setDeploymentName(deploymentName).setBuildId(deploymentVersion).build())
                        .build());
    }


    private static WorkerFactory startWorkerInVersion(WorkflowClient client, String deploymentVersionV1, Class<?> aClass) {
        WorkerFactory factory = WorkerFactory.newInstance(client);
        WorkerOptions builder = WorkerOptions.newBuilder()
                .setDeploymentOptions(
                        WorkerDeploymentOptions.newBuilder()
                                .setUseVersioning(true)
                                .setVersion(WorkerDeploymentVersion.fromCanonicalString(deploymentName + "." + deploymentVersionV1))
                                .setDefaultVersioningBehavior(VersioningBehavior.AUTO_UPGRADE)
                                .build())
                .build();

        Worker worker = factory.newWorker(TASK_QUEUE, builder);

        worker.registerWorkflowImplementationTypes(aClass);

        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
        factory.start();


        return factory;
    }

    private static void setCurrentVersionLoan(WorkflowClient client, String deploymentVersion) {
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

                    System.out.println("Deployment info for task queue " + TASK_QUEUE + ", deployment version " + deploymentName + ":");

                    ListWorkflowExecutionsResponse listWorkflowExecutionsResponse = client.getWorkflowServiceStubs().blockingStub().listWorkflowExecutions(ListWorkflowExecutionsRequest.newBuilder()
                            .setNamespace(namespace)
                            .setQuery("`TemporalWorkerDeploymentVersion`=\"" + deploymentName + ":" + deploymentVersion_v1 + "\" AND `ExecutionStatus`=\"Running\"")
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

                    System.out.println("- TaskQueue Versions info: \n" + taskqueue.getVersioningInfo().getCurrentDeploymentVersion());


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

    private static WorkflowExecution startWorkflow(WorkflowClient client, int iterations, String workflowId) {

        System.out.println("\n*********Starting Workflow***********\n");

        // Create the workflow client stub. It is used to start our workflow execution.
        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(workflowId)
                                .setTaskQueue(TASK_QUEUE)
                                .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING)
                                .build());


        return WorkflowClient.start(workflow::mainMethod, "", iterations);
    }

}
