package com.temporal.demos.temporalspringbootdemo;

import com.temporal.demos.temporalspringbootdemo.workflows.DemoWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

// @Configuration
public class InitWorker {

    public InitWorker() {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker("HelloSampleTaskQueue");


        worker.registerWorkflowImplementationTypes(DemoWorkflowImpl.class);

        worker.registerActivitiesImplementations(new com.temporal.demos.temporalspringbootdemo.workflows.activity_v2.MyActivityImpl());
        worker.registerActivitiesImplementations(new com.temporal.demos.temporalspringbootdemo.workflows.activity.MyActivityImpl());

        factory.start();
    }
}
