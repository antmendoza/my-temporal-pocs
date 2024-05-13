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

package io.antmendoza.samples._5859;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.time.Duration;

public class Client_5859 {

    // Define the task queue name
    static final String TASK_QUEUE = Client_5859.class.getName();

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloContextPropagatorWorkflow";

    
    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        final String traceId = "my-trace-id"+Math.random();

        WorkflowClient client =
                WorkflowClient.newInstance(
                        service,
                        WorkflowClientOptions.newBuilder()
                                //.setContextPropagators(Collections.singletonList(new MyContextPropagator()))
                                .build());

        // Create the workflow client stub. It is used to start our workflow execution.
        GreetingWorkflow_5859 workflow =
                client.newWorkflowStub(
                        GreetingWorkflow_5859.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setWorkflowRunTimeout(Duration.ofSeconds(100)) // just for testing purpose
                                .setTaskQueue(TASK_QUEUE)
                                .build());

        String greeting = workflow.getGreeting();

        System.out.println(greeting);


        System.exit(0);
    }

}
