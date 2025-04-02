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
import io.temporal.internal.common.WorkflowExecutionHistory;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.testing.WorkflowReplayer;
import org.junit.Rule;
import org.junit.Test;

public class HelloActivityReplayTest {

    @Rule
    public TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder().setDoNotStart(true).build();

    @Test
    public void replayWorkflowExecution() throws Exception {

        final String eventHistory = executeWorkflow(GreetingWorkflowImpl.class);

        WorkflowReplayer.replayWorkflowExecution(
                eventHistory, GreetingWorkflowImpl.class);
    }


    private String executeWorkflow(
            Class<? extends GreetingWorkflow> workflowImplementationType) {

        testWorkflowRule
                .getWorker()
                .registerActivitiesImplementations(new GreetingActivitiesImpl());

        testWorkflowRule.getWorker().registerWorkflowImplementationTypes(workflowImplementationType);

        testWorkflowRule.getTestEnvironment().start();

        GreetingWorkflow workflow =
                testWorkflowRule
                        .getWorkflowClient()
                        .newWorkflowStub(
                                GreetingWorkflow.class,
                                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
        WorkflowExecution execution = WorkflowStub.fromTyped(workflow).start("Hello");
        // wait until workflow completes
        WorkflowStub.fromTyped(workflow).getResult(String.class);

        return new WorkflowExecutionHistory(testWorkflowRule.getHistory(execution)).toJson(true);
    }

}
