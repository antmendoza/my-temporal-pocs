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

package io.temporal.samples.hello;

import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.client.WorkflowUpdateStage;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class Runner_2 {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";


    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

        factory.start();


        // Create the workflow client stub. It is used to start our workflow execution.
        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING)
                                .build());


        WorkflowClient.start(workflow::mainMethod, "");


        sleepMs(1000);

        final WorkflowStub workflowStub = WorkflowStub.fromTyped(workflow);
        workflowStub
                .startUpdate("getNotification",
                        WorkflowUpdateStage.ACCEPTED,
                        String.class, "1");


        sleepMs(1000);


        workflowStub
                .signal("aggregateNotification",
                        Void.class);

        // wait workflow execution results
        workflowStub.getResult(String.class);


        //System.exit(0);
    }

    private static void sleepMs(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
