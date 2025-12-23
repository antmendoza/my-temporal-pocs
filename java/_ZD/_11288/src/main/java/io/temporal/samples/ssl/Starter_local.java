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

package io.temporal.samples.ssl;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.util.AdvancedTlsX509KeyManager;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.ResetWorkflowExecutionRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Starter_local {

  static final String TASK_QUEUE = "MyTaskQueue";
  static final String WORKFLOW_ID = "HelloSSLWorkflow";

  public static void main(String[] args) throws Exception {


    // Create SSL enabled client by passing SslContext
    WorkflowServiceStubs service =
        WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                .build());

    // Now setup and start workflow worker, which uses SSL enabled gRPC service to communicate with
    // backend.
    // client that can be used to start and signal workflows.
    WorkflowClient client =
        WorkflowClient.newInstance(
            service, WorkflowClientOptions.newBuilder().build());

    WorkerFactory factory = WorkerFactory.newInstance(client);

    Worker worker = factory.newWorker(TASK_QUEUE);



    worker.registerWorkflowImplementationTypes(
        MyWorkflowImpl.class,
        HelloChild.GreetingWorkflowImpl.class,
        HelloChild.GreetingChildImpl.class);

    factory.start();

    HelloChild.GreetingWorkflow workflow =
        client.newWorkflowStub(
            HelloChild.GreetingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .build());

    WorkflowClient.start(workflow::getGreeting, "John");



    Thread.sleep(1000);

    if(true){

      client
              .getWorkflowServiceStubs()
              .blockingStub()
              .resetWorkflowExecution(
                      ResetWorkflowExecutionRequest.newBuilder()
                              .setRequestId("" + System.currentTimeMillis())
                              .setNamespace("default")
                              .setWorkflowTaskFinishEventId(4)
                              .setWorkflowExecution(
                                      WorkflowExecution.newBuilder().setWorkflowId(WORKFLOW_ID).build())
                              .build());


    }



    Thread.sleep(1000);

  }
}
