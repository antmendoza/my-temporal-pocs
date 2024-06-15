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

import com.antmendoza.generator.workflow.MyWorkflow;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class MyStarter {
    public static final WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    public static final String TASK_QUEUE_NAME = "tracingTaskQueue";

    public static void main(String[] args) {


        start();

        //System.exit(0);
    }

    public static void start() {
        // Set the OpenTracing client interceptor
        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);

        WorkflowOptions workflowOptions =
                WorkflowOptions.newBuilder()
                        .setWorkflowId("hello-translator")
                        .setTaskQueue(TASK_QUEUE_NAME)
                        .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_TERMINATE_IF_RUNNING)
                        .build();

        // Create typed workflow stub
        MyWorkflow workflow = client.newWorkflowStub(MyWorkflow.class, workflowOptions);

        // Convert to untyped and start it with signalWithStart
        WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
        untyped.signalWithStart("setLanguage", new Object[]{"Spanish"}, new Object[]{"John"});

        String greeting = untyped.getResult(String.class);
    }


}
