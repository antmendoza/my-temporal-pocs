package com.antmendoza.temporal;

import com.antmendoza.temporal.workflow.MyActivityImpl;
import com.antmendoza.temporal.workflow.MyWorkflowCANImpl;
import com.antmendoza.temporal.workflow.MyWorkflowRunForeverImpl;
import com.antmendoza.temporal.workflow.SlowDataConverter;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;


import static com.antmendoza.temporal.Starter.TASK_QUEUE;


public class MyWorker {

    public static void main(String[] args) throws Exception {


        WorkflowClient client = getWorkflowClient();


        WorkerFactory factory = WorkerFactory.newInstance(client, WorkerFactoryOptions.newBuilder()
                //Intentionally we set cache low to force workflow replay while
                .setWorkflowCacheSize(4)
                .build());


        Worker worker = factory.newWorker(TASK_QUEUE, WorkerOptions.newBuilder()
                        .setDisableEagerExecution(true)
                .build());

        worker.registerWorkflowImplementationTypes(
                MyWorkflowCANImpl.class,
                MyWorkflowRunForeverImpl.class
        );



        worker.registerActivitiesImplementations(new MyActivityImpl());
        factory.start();

    }

    public static WorkflowClient getWorkflowClient() {
        final WorkflowServiceStubsOptions.Builder builder = WorkflowServiceStubsOptions.newBuilder();

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        builder
                                .build());


        WorkflowClient client =
                WorkflowClient.newInstance(
                        service, WorkflowClientOptions.newBuilder()
                                //Data converter simulating time consuming operation
                                .setDataConverter(new SlowDataConverter())
                                .build());
        return client;
    }


}
