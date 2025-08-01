/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package com.antmendoza.temporal;

import com.antmendoza.temporal.config.SslContextBuilderProvider;
import com.antmendoza.temporal.workflow.MyActivityImpl;
import com.antmendoza.temporal.workflow.MyWorkflow1;
import com.antmendoza.temporal.workflow.MyWorkflow1Impl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Runner {

    public static final String TASK_QUEUE = "MyTaskQueue_";


    public static void main(String[] args) throws InterruptedException {

        SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();


        final WorkflowServiceStubsOptions.Builder builder = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(sslContextBuilderProvider.properties.getTemporalStarterTargetEndpoint());

        if (sslContextBuilderProvider.getSslContext() != null) {
            builder.setSslContext(sslContextBuilderProvider.getSslContext());
        }

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        builder
                                .build());


        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setNamespace(sslContextBuilderProvider.properties.getTemporalNamespace())
                        .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);


        //Start worker
        CompletableFuture.runAsync(() -> {

            final MyActivityImpl myActivity = new MyActivityImpl();
            WorkerFactory factory = startWorkerWithMaxTaskQueueActivitiesPerSecond(client, 50, myActivity);
            factory.start();


            sleep(60_000);
            //factory.suspendPolling();
            factory.shutdown();
            factory.awaitTermination(5, TimeUnit.SECONDS);

            factory = startWorkerWithMaxTaskQueueActivitiesPerSecond(client, 10, myActivity);
            factory.start();


        });



        WorkflowOptions workflowOptions =
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .build();


        MyWorkflow1 workflow = client.newWorkflowStub(MyWorkflow1.class, workflowOptions);
        workflow.run(10, 0);

        System.exit(0);

    }

    private static WorkerFactory startWorkerWithMaxTaskQueueActivitiesPerSecond(WorkflowClient client, int maxTaskQueueActivitiesPerSecond, MyActivityImpl myActivity) {
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE,
                WorkerOptions
                        .newBuilder()
                        .setMaxTaskQueueActivitiesPerSecond(maxTaskQueueActivitiesPerSecond)
                        .setMaxConcurrentActivityExecutionSize(500)
                        .setMaxConcurrentWorkflowTaskExecutionSize(500)
                        .setMaxConcurrentActivityTaskPollers(100)
                        .setMaxConcurrentWorkflowTaskPollers(100)
                        .build());
        worker.registerWorkflowImplementationTypes(MyWorkflow1Impl.class);
        worker.registerActivitiesImplementations(myActivity);
        return factory;
    }

    private static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
