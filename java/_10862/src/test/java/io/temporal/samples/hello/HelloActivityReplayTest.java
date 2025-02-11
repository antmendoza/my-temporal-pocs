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

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.common.WorkflowExecutionHistory;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.testing.WorkflowReplayer;
import org.junit.Rule;
import org.junit.Test;


public class HelloActivityReplayTest {


    static final String WORKFLOW_ID = "HelloActivityWorkflow";


    @Rule
    public TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder()
                    .setDoNotStart(true).build();



    @Test
    public void replayWorkflowExecution() throws Exception {

        final String eventHistory = executeWorkflow(HelloActivityV1.GreetingWorkflowImpl.class);

        WorkflowReplayer.replayWorkflowExecution(
                eventHistory, HelloActivityV1.GreetingWorkflowImpl.class);
    }


    @Test
    public void replayWorkflowExecution_with_HelloActivityV2_add_string() throws Exception {

        final String eventHistory = executeWorkflow(HelloActivityV1.GreetingWorkflowImpl.class);

        //Replay with HelloActivityV2_add_string
        WorkflowReplayer.replayWorkflowExecution(
                eventHistory, HelloActivityV2_add_string.GreetingWorkflowImpl.class);
    }


    @Test
    public void replayWorkflowExecution_with_HelloActivityV3_add_list_strings() throws Exception {

        final String eventHistory = executeWorkflow(HelloActivityV1.GreetingWorkflowImpl.class);

        //Replay with HelloActivityV3_add_list_strings
        WorkflowReplayer.replayWorkflowExecution(
                eventHistory, HelloActivityV3_add_list_strings.GreetingWorkflowImpl.class);
    }


    @Test
    public void replayWorkflowExecution_with_HelloActivityV4_add_object() throws Exception {

        final String eventHistory = executeWorkflow(HelloActivityV1.GreetingWorkflowImpl.class);

        //Replay with HelloActivityV4_add_object
        WorkflowReplayer.replayWorkflowExecution(
                eventHistory, HelloActivityV4_add_object.GreetingWorkflowImpl.class);
    }



    private String executeWorkflow(Class<?> workflowImplementationType) throws InterruptedException {


        testWorkflowRule
                .getWorker();


        testWorkflowRule.getWorker().registerWorkflowImplementationTypes(workflowImplementationType);

        testWorkflowRule.getTestEnvironment().start();

        HelloActivityV1.GreetingWorkflow workflow =
                testWorkflowRule
                        .getWorkflowClient()
                        .newWorkflowStub(
                                HelloActivityV1.GreetingWorkflow.class,
                                WorkflowOptions.newBuilder()
                                        .setWorkflowId(WORKFLOW_ID + Math.random())
                                        .setTaskQueue(testWorkflowRule.getTaskQueue()).build());



        WorkflowExecution execution = WorkflowStub.fromTyped(workflow).start("Hello");


        Thread.sleep(500);
        WorkflowStub.fromTyped(workflow).signal("signal", "anything");

        Thread.sleep(500);
        WorkflowStub.fromTyped(workflow).getResult(String.class);

        return new WorkflowExecutionHistory(testWorkflowRule.getHistory(execution)).toJson(true);
    }


}
