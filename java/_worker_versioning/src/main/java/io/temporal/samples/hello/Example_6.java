package io.temporal.samples.hello;

import com.google.protobuf.FieldMask;
import io.temporal.api.batch.v1.BatchOperationUpdateWorkflowExecutionOptions;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.api.taskqueue.v1.TaskQueue;
import io.temporal.api.workflow.v1.WorkflowExecutionOptions;
import io.temporal.api.workflowservice.v1.*;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.VersioningBehavior;
import io.temporal.common.VersioningOverride;
import io.temporal.common.WorkerDeploymentVersion;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerDeploymentOptions;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;

import java.util.concurrent.CompletableFuture;

public class Example_6 {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue" + System.currentTimeMillis();

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";

    static final String namespace = "default";

    static final String deploymentName = "loan-proces" + System.currentTimeMillis();

    static final String deploymentVersionV1 = "v1";
    static final String deploymentVersionV2 = "v2";


    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs
                .newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .build());

        WorkflowClient client = WorkflowClient.newInstance(service);


        listExecutions(client);

        startWorkflow(client, 100);

        {
            WorkerFactory factory = WorkerFactory.newInstance(client);
            WorkerOptions builder = WorkerOptions.newBuilder()
                    .setDeploymentOptions(
                            WorkerDeploymentOptions.newBuilder()
                                    .setUseVersioning(true)
                                    .setVersion(WorkerDeploymentVersion.fromCanonicalString(deploymentName + "." + deploymentVersionV1))
                                    .setDefaultVersioningBehavior(VersioningBehavior.PINNED)
                                    .build())
                    .build();

            Worker worker = factory.newWorker(TASK_QUEUE, builder);
            worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
            worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
            factory.start();

        }
        System.out.println("\n********Worker started with version " + deploymentName + " : " + deploymentVersionV1 + " ***********\n");


        // Sleep to allow workflow to iterate a few times
        sleep(5_000);

        setCurrentVersionLoan(client, deploymentVersionV1);


        {
            WorkerFactory factory = WorkerFactory.newInstance(client);
            WorkerOptions builder = WorkerOptions.newBuilder()
                    .setDeploymentOptions(
                            WorkerDeploymentOptions.newBuilder()
                                    .setUseVersioning(true)
                                    .setVersion(WorkerDeploymentVersion.fromCanonicalString(deploymentName + "." + deploymentVersionV2))
                                    .setDefaultVersioningBehavior(VersioningBehavior.PINNED)
                                    .build())
                    .build();

            Worker worker = factory.newWorker(TASK_QUEUE, builder);

            worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

            worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
            factory.start();

        }

        System.out.println("\n********Worker started with version " + deploymentName + " : " + deploymentVersionV2 + " ***********\n");

        // Sleep to allow workflow to iterate a few times
        sleep(5_000);


        setRampingVersionLoan(client, deploymentVersionV2, 20);

        sleep(5_000);

        movePinnedWorkflowsToAutoupgrade(client);
        sleep(5_000);

        setRampingVersionLoan(client, deploymentVersionV2, 50);

        sleep(5_000);

        setRampingVersionLoan(client, deploymentVersionV2, 100);
        sleep(5_000);


        setCurrentVersionLoan(client, deploymentVersionV2);




    }

    private static void movePinnedWorkflowsToAutoupgrade(WorkflowClient client) {
        client.getWorkflowServiceStubs().blockingStub().startBatchOperation(
                StartBatchOperationRequest.newBuilder()
                        .setNamespace(namespace)
                        .setReason("Upgrade workflows to new deployment version")
                        .setJobId(System.currentTimeMillis() + "")
                        .setVisibilityQuery("TemporalWorkerDeployment=\"" + deploymentName + "\"")

                        .setUpdateWorkflowOptionsOperation(BatchOperationUpdateWorkflowExecutionOptions.newBuilder()
                                .setWorkflowExecutionOptions(WorkflowExecutionOptions.newBuilder()
                                        .setVersioningOverride(io.temporal.api.workflow.v1.VersioningOverride.newBuilder()
                                                .setAutoUpgrade(true).build())
                                        .build())

                                .setUpdateMask(
                                        FieldMask.newBuilder()
                                                // Path is relative to WorkflowExecutionOptions
                                                .addPaths("versioning_override")
                                                .build())
                                .build())


                        .build()

        );

        System.out.println("\n********Move pinned workflows to autoupgrade for deployment " + deploymentName + " ***********\n");

    }

    private static void setCurrentVersionLoan(WorkflowClient client, String deploymentVersion) {
        client.getWorkflowServiceStubs().blockingStub()
                .setWorkerDeploymentCurrentVersion(SetWorkerDeploymentCurrentVersionRequest.newBuilder()
                        .setNamespace(namespace)
                        .setDeploymentName(deploymentName)
                        .setBuildId(deploymentVersion).build());

        System.out.println("\n********Promote deployment version " + deploymentName + ":" + deploymentVersion + " to current***********\n");

    }

    private static void setRampingVersionLoan(WorkflowClient client, String deploymentVersion, int percentage) {
        client.getWorkflowServiceStubs().blockingStub()
                .setWorkerDeploymentRampingVersion(SetWorkerDeploymentRampingVersionRequest.newBuilder()
                        .setNamespace(namespace)
                        .setDeploymentName(deploymentName)
                        .setPercentage(percentage)
                        .setBuildId(deploymentVersion).build());

        System.out.println("\n********Set ramping version to  " + deploymentName + ":" + deploymentVersion + " with " + percentage + " percentage ***********\n");

    }


    private static void listExecutions(WorkflowClient client) {


        CompletableFuture.runAsync(() -> {
            while (true) {

                try {


                    sleep(3_000);

                    System.out.println("\n********Deployment info for task queue " + TASK_QUEUE + ", deployment name " + deploymentName + ":");


                    DescribeTaskQueueResponse taskqueue = client.getWorkflowServiceStubs().blockingStub().describeTaskQueue(
                            DescribeTaskQueueRequest.newBuilder()
                                    .setReportStats(true)
                                    .setNamespace(namespace)
                                    .setTaskQueue(TaskQueue.newBuilder().setName(TASK_QUEUE).build()).build());

                    System.out.println("- Current TaskQueue Version info: \n" + taskqueue.getVersioningInfo().getCurrentDeploymentVersion());


                    taskqueue.getVersionsInfoMap().forEach((v, k) -> {
                        //System.out.println("  - Version: " + v + " , Executions: " + k);
                    });


                    for (String version : new String[]{deploymentVersionV1, deploymentVersionV2}) {


                        ListWorkflowExecutionsResponse listWorkflowExecutionsResponse = client.getWorkflowServiceStubs().blockingStub().listWorkflowExecutions(ListWorkflowExecutionsRequest.newBuilder()
                                .setNamespace(namespace)
                                .setQuery("`TemporalWorkerDeploymentVersion`=\"" + deploymentName + ":" + version + "\" AND `ExecutionStatus`=\"Running\"")
                                .build());


                        System.out.println("- Num running executions in worker deployment version " + version + ": " + listWorkflowExecutionsResponse.getExecutionsCount());

                        listWorkflowExecutionsResponse.getExecutionsList().forEach(v -> {
                            //                       System.out.println("    - Found execution with " + deploymentName + ":"+deploymentVersion+" " + v.getExecution().getWorkflowId());
                        });

                    }


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

    private static void startWorkflow(WorkflowClient client, int number) {

        System.out.println("\n*********Starting  " + number + " Workflows ***********\n");

        for (int i = 0; i < number; i++) {


            String workflowId = WORKFLOW_ID + i;

            // Create the workflow client stub. It is used to start our workflow execution.
            WorkflowOptions.Builder builder = WorkflowOptions.newBuilder()
                    .setWorkflowId(workflowId)
                    .setTaskQueue(TASK_QUEUE)
                    .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING);


            GreetingWorkflow workflow =
                    client.newWorkflowStub(
                            GreetingWorkflow.class,
                            builder
                                    .build());


            WorkflowClient.start(workflow::mainMethod, "");


        }
    }

}
