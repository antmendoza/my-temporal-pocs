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
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.Duration;

/**
 * Sample Temporal Workflow Definition that demonstrates the execution of a Child Workflow. Child
 * workflows allow you to group your Workflow logic into small logical and reusable units that solve
 * a particular problem. They can be typically reused by multiple other Workflows.
 */
public class StarterHelloChild {

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



        // Start a workflow execution. Usually this is done from another program.
        // Uses task queue from the GreetingWorkflow @WorkflowMethod annotation.

        // Create our parent workflow client stub. It is used to start the parent workflow execution.
        HelloChild.GreetingWorkflow workflow =
                client.newWorkflowStub(
                        HelloChild.GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                .build());

        // Execute our parent workflow and wait for it to complete.
        String greeting = workflow.getGreeting("World");

        // Display the parent workflow execution results
        System.out.println(greeting);
        System.exit(0);
    }

}
