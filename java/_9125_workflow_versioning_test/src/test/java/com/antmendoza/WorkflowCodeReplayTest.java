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

import static com.antmendoza.WorkflowCode.createAsyncChildWorkflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.common.WorkflowExecutionHistory;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.testing.WorkflowReplayer;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

public class WorkflowCodeReplayTest {

  @Rule
  public TestWorkflowRule testWorkflowRule =
      TestWorkflowRule.newBuilder()
          // .setNamespace("default")
          // .setUseExternalService(true)
          .setDoNotStart(true)
          .build();

  @AfterEach
  public void after() {
    testWorkflowRule.getTestEnvironment().shutdown();
  }

  @Test
  public void replayWorkflowExecution() throws Exception {

    final Class<WorkflowCode.MyWorkflowImpl> workflowImplementationType =
        WorkflowCode.MyWorkflowImpl.class;

    createWorker(WorkflowCode.MyWorkflowImpl.class);
    final String workflowId = executeMainWorkflow();

    final String eventHistory = getWorkflowExecutionHistory(workflowId).toJson(true);

    WorkflowReplayer.replayWorkflowExecution(eventHistory, workflowImplementationType);
  }

  @Test
  public void replayWorkflowExecutionWithVersionAndGetChild() throws Exception {

    createWorker(WorkflowCode.MyWorkflowImpl.class);
    final String workflowId = executeMainWorkflow();

    final String eventHistory = getWorkflowExecutionHistory(workflowId).toJson(true);

    WorkflowReplayer.replayWorkflowExecution(
        eventHistory, MyWorkflowImplWithGetChildPromiseVersioned.class);
  }

  private String executeMainWorkflow() {

    WorkflowCode.MyWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                WorkflowCode.MyWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
    WorkflowExecution execution = WorkflowStub.fromTyped(workflow).start("World");
    // wait until workflow completes
    String result = WorkflowStub.fromTyped(workflow).getResult(String.class);

    Assert.assertEquals("Hello World!", result);

    return execution.getWorkflowId();
  }

  private void createWorker(
      final Class<? extends WorkflowCode.MyWorkflow> workflowImplementationType) {
    testWorkflowRule
        .getWorker()
        .registerActivitiesImplementations(new WorkflowCode.GreetingActivitiesImpl());

    testWorkflowRule
        .getWorker()
        .registerWorkflowImplementationTypes(
            workflowImplementationType, WorkflowCode.MyChildWorkflowImpl.class);

    testWorkflowRule.getTestEnvironment().start();
  }

  public static class MyWorkflowImplWithGetChildPromiseVersioned
      implements WorkflowCode.MyWorkflow {

    private final WorkflowCode.GreetingActivities activities =
        Workflow.newActivityStub(
            WorkflowCode.GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String getGreeting(String name) {

      final String childWorkflow1 = "child_workflow_1_" + Workflow.currentTimeMillis();

      final WorkflowCode.MyChildWorkflow child_1 = createAsyncChildWorkflow(name, childWorkflow1);
      // Wait for child to start
      Workflow.getWorkflowExecution(child_1).get();

      //      1	call an activity
      // This is a blocking call that returns only after the activity has completed.
      final String hello = activities.composeGreeting("Hello", name);

      //      2	signal external workflow
      Workflow.newUntypedExternalWorkflowStub(childWorkflow1).signal("signal_1", "value_1");

      //      3	start child workflow using Async.function
      final String childWorkflow2 = "child_workflow_2_" + Workflow.currentTimeMillis();
      final WorkflowCode.MyChildWorkflow child_2 = createAsyncChildWorkflow(name, childWorkflow2);

      //      4	use getVersion
      int version = Workflow.getVersion("get-child-workflow", Workflow.DEFAULT_VERSION, 1);

      //      5	query execution details of the started child workflow in step 3 if version == 1
      // (i.e. not default version)
      if (version == 1) {
        // Wait for child to start
        Workflow.getWorkflowExecution(child_2).get();
      }

      return hello;
    }
  }

  private WorkflowExecutionHistory getWorkflowExecutionHistory(final String workflowId) {
    return new WorkflowExecutionHistory(
        testWorkflowRule.getHistory(
            WorkflowExecution.newBuilder().setWorkflowId(workflowId).build()));
  }
}
