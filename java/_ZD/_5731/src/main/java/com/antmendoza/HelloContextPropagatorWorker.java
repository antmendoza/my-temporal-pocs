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

package com.antmendoza;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import java.util.*;

public class HelloContextPropagatorWorker {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloContextPropagatorTaskQueue";

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloContextPropagatorWorkflow";


    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        startWorker(service);


    }

    private static void startWorker(final WorkflowServiceStubs service) {
        /*
         * Get a Workflow service client which can be used to start, Signal, and Query Workflow Executions.
         */
        WorkflowClient client =
                WorkflowClient.newInstance(
                        service,
                        WorkflowClientOptions.newBuilder()
                                .setContextPropagators(List.of(new MyContextPropagator()))
                                .build());

        // MDC.put("X-", "testing123");

        /*
         * Define the workflow factory. It is used to create workflow workers for a specific task queue.
         */
        WorkerFactory factory = WorkerFactory.newInstance(client);

        /*
         * Define the workflow worker. Workflow workers listen to a defined task queue and process
         * workflows and activities.
         */
        Worker worker = factory.newWorker(TASK_QUEUE);

        /*
         * Register our workflow implementation with the worker.
         * Workflow implementations must be known to the worker at runtime in
         * order to dispatch workflow tasks.
         */
        worker.registerWorkflowImplementationTypes(GreetingWorkflow.GreetingWorkflowImpl.class);

        /*
         * Register our Activity Types with the Worker. Since Activities are stateless and thread-safe,
         * the Activity Type is a shared instance.
         */
        worker.registerActivitiesImplementations(new GreetingWorkflow.GreetingWorkflowImpl.GreetingActivitiesImpl());

        factory.start();
    }




}
