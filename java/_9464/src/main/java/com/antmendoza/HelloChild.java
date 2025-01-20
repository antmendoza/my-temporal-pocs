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
public class HelloChild {

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
        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);


        /*
         * Start all the workers registered for a specific task queue.
         * The started workers then start polling for workflows and activities.
         */
        factory.start();

        // Start a workflow execution. Usually this is done from another program.
        // Uses task queue from the GreetingWorkflow @WorkflowMethod annotation.

        // Create our parent workflow client stub. It is used to start the parent workflow execution.
        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
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

    /**
     * Define the parent workflow interface. It must contain one method annotated with @WorkflowMethod
     *
     * @see WorkflowInterface
     * @see WorkflowMethod
     */
    @WorkflowInterface
    public interface GreetingWorkflow {

        /**
         * Define the parent workflow method. This method is executed when the workflow is started. The
         * workflow completes when the workflow method finishes execution.
         */
        @WorkflowMethod
        String getGreeting(String name);
    }

    // Define the parent workflow implementation. It implements the getGreeting workflow method
    public static class GreetingWorkflowImpl implements GreetingWorkflow {

        @Override
        public String getGreeting(String name) {

            // Get the version of the workflow
            int version = Workflow.getVersion("sample-change", Workflow.DEFAULT_VERSION, 2);

            if (version == Workflow.DEFAULT_VERSION) {
                // Old logic
                System.out.println("version DEFAULT_VERSION " + version);
            } else if (version == 1) {
                // New logic for version 1
                System.out.println("version 1 " + version);
            } else if (version == 2) {
                // New logic for version 2
                System.out.println("version 2 " + 2);
            }

            Workflow.sleep(Duration.ofSeconds(20));
            //Workflow.continueAsNew(name);

            // Wait for the child workflow to complete and return its results
            return "done";
        }
    }

}
