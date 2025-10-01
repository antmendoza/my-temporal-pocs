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
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.client.WorkflowUpdateStage;
import io.temporal.common.interceptors.*;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.worker.WorkerFactoryOptions;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HelloActivityTest {




    public class MyWorkerInterceptor extends WorkerInterceptorBase {

        private final List<String> steps;

        public MyWorkerInterceptor() {
            this.steps = new ArrayList<>();
        }


        public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {
            return new MyInterceptor(next, steps);
        }

        public boolean hasStep(String step) {
            return steps.contains(step);
        }
    }

    public static class MyInterceptor extends WorkflowInboundCallsInterceptorBase {

        private final WorkflowInboundCallsInterceptor next;
        private final List<String> steps;

        public MyInterceptor(WorkflowInboundCallsInterceptor next, List<String> steps) {
            super(next);
            this.next = next;
            this.steps = steps;
        }

        @Override
        public UpdateOutput executeUpdate(UpdateInput input) {


            UpdateOutput updateOutput = next.executeUpdate(input);

            this.steps.add(input.getUpdateName());
            return updateOutput;
        }


    }


    private MyWorkerInterceptor interceptor = new MyWorkerInterceptor();
    @Rule
    public TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder()
                    .setWorkerFactoryOptions(WorkerFactoryOptions.newBuilder()
                            .setWorkerInterceptors(interceptor)
                            .build())
                    .setDoNotStart(true).build();

    @Test
    public void replayWorkflowExecution() throws Exception {




        testWorkflowRule.getWorker().registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

        testWorkflowRule.getTestEnvironment().start();

        GreetingWorkflow workflow =
                testWorkflowRule
                        .getWorkflowClient()
                        .newWorkflowStub(
                                GreetingWorkflow.class,
                                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());


        final WorkflowStub workflowStub = WorkflowStub.fromTyped(workflow);


        WorkflowExecution execution = workflowStub.start("Hello");


        assertFalse(interceptor.hasStep("getNotification"));

        workflowStub
                .startUpdate("getNotification",
                        WorkflowUpdateStage.COMPLETED,
                        String.class, "1");


        assertTrue(interceptor.hasStep("getNotification"));


        workflowStub.getResult(String.class);



    }







}
