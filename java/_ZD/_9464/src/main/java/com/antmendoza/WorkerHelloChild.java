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
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

/**
 * Sample Temporal Workflow Definition that demonstrates the execution of a Child Workflow. Child
 * workflows allow you to group your Workflow logic into small logical and reusable units that solve
 * a particular problem. They can be typically reused by multiple other Workflows.
 */
public class WorkerHelloChild {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloChildTaskQueue";

    // Define the workflow unique id
    static final String WORKFLOW_ID = "HelloChildWorkflow";

    /**
     * With the workflow, and child workflow defined, we can now start execution. The main method is
     * the workflow starter.
     */
    public static void main(String[] args) {

        // Get a Workflow service stub.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        /*
         * Get a Workflow service client which can be used to start, Signal, and Query Workflow Executions.
         */
        WorkflowClient client = WorkflowClient.newInstance(service);

        /*
         * Define the worker factory. It is used to create workers for a specific task queue.
         */
        WorkerFactory factory = WorkerFactory.newInstance(client);

        /*
         * Define the worker. Workers listen to a defined task queue and process workflows and
         * activities.
         */
        Worker worker = factory.newWorker(TASK_QUEUE);

        /*
         * Register the parent and child workflow implementation with the worker.
         * Since workflows are stateful in nature,
         * we need to register the workflow types.
         */
        worker.registerWorkflowImplementationTypes(HelloChild.GreetingWorkflowImpl.class);


        /*
         * Start all the workers registered for a specific task queue.
         * The started workers then start polling for workflows and activities.
         */
        factory.start();

    }


}
