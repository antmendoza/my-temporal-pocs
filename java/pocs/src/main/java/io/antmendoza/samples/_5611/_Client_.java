package io.antmendoza.samples._5611;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import java.time.Instant;

public class _Client {

    public static String TASK_QUEUE = "my-task-queue";


    public static void main(String[] args) {

        final WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(Workflow_5611Impl_.class);
        worker.registerActivitiesImplementations(new Activity_5611Impl());

        factory.start();


        final String workflowId = "my-kotlin";
        final WorkflowOptions build = WorkflowOptions
                .newBuilder()
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowId(workflowId +  Math.random())
                .build();



        Workflow_5611 workflow = client
                .newWorkflowStub(Workflow_5611.class,
                        build);

        System.out.println("Clock: "+ Instant.now());

        WorkflowExecution execution = WorkflowClient.start(workflow::run);


        String result = client.newUntypedWorkflowStub(workflowId).getResult(String.class);

        System.out.println("Result : " + result);
        System.out.println("Clock: "+ Instant.now());


    }


}