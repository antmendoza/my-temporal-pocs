package com.antmendoza.temporal;

import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.interceptors.WorkerInterceptor;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;

public class Starter {

    static final String TASK_QUEUE = "ActivityInterceptorTaskQueue";
    static final String WORKFLOW_ID = "ActivityInterceptorWorkflow";

    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactoryOptions factoryOptions =
                WorkerFactoryOptions.newBuilder()
                        .setWorkerInterceptors(
                                new WorkerInterceptor[]{
                                        new SimpleActivityInterceptor(), new SimpleWorkflowInterceptor()
                                })
                        .build();

        WorkerFactory factory = WorkerFactory.newInstance(client, factoryOptions);

        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

        factory.start();

        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING)
                                .setTaskQueue(TASK_QUEUE)
                                .build());

        String greeting = workflow.getGreeting("World");

        System.out.println("Workflow result: " + greeting);
        System.exit(0);
    }
}
