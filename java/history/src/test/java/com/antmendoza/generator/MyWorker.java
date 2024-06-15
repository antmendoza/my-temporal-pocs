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

package com.antmendoza.generator;

import com.antmendoza.generator.workflow.ActivitiesImpl;
import com.antmendoza.generator.workflow.MyWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;

public class MyWorker {
  private static final WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
  private static final WorkflowClient client = WorkflowClient.newInstance(service);
  public static final String TASK_QUEUE_NAME = "tracingTaskQueue";

  public static void main(String[] args) {

    runWorker();
  }

  public static void runWorker() {
    // Set the OpenTracing client interceptor
    WorkerFactoryOptions factoryOptions =
        WorkerFactoryOptions.newBuilder()
            .build();
    WorkerFactory factory = WorkerFactory.newInstance(client, factoryOptions);

    Worker worker = factory.newWorker(TASK_QUEUE_NAME, WorkerOptions.newBuilder()
            .build());
    worker.registerWorkflowImplementationTypes(
        MyWorkflowImpl.class);
    worker.registerActivitiesImplementations(new ActivitiesImpl());

    factory.start();
  }
}
