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

import static org.hamcrest.MatcherAssert.assertThat;

import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.common.WorkflowExecutionHistory;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.testing.WorkflowReplayer;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.hamcrest.CoreMatchers;
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

  private static WorkflowCode.GreetingActivities createActivityStub() {
    return Workflow.newActivityStub(
        WorkflowCode.GreetingActivities.class,
        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());
  }

  @AfterEach
  public void after() {
    testWorkflowRule.getTestEnvironment().shutdown();
  }

  @Test
  public void replayWorkflowExecution() throws Exception {

    final Class<WorkflowCode.MyWorkflowImpl> workflowImplementationType =
        WorkflowCode.MyWorkflowImpl.class;
    final String workflowId = executeWorkflow(workflowImplementationType);

    final String eventHistory = getWorkflowExecutionHistory(workflowId).toJson(true);

    WorkflowReplayer.replayWorkflowExecution(eventHistory, workflowImplementationType);
  }

  @Test
  public void replayWorkflowExecutionWithNonDeterministicCode() {

    try {

      final String workflowId = executeWorkflow(WorkflowCode.MyWorkflowImpl.class);

      final String eventHistory = getWorkflowExecutionHistory(workflowId).toJson(true);

      WorkflowReplayer.replayWorkflowExecution(
          eventHistory, WorkflowImplWithTimerNotVersioned.class);

      Assert.fail("Should have thrown an Exception");
    } catch (Exception e) {
      assertThat(
          e.getMessage(),
          CoreMatchers.containsString("error=io.temporal.worker.NonDeterministicException"));
    }
  }

  @Test
  public void replayWorkflowExecutionWithVersionOnly() throws Exception {

    final String workflowId = executeWorkflow(WorkflowCode.MyWorkflowImpl.class);

    final String eventHistory = getWorkflowExecutionHistory(workflowId).toJson(true);

    WorkflowReplayer.replayWorkflowExecution(eventHistory, MyWorkflowImplWithVersionOnly.class);
  }

  @Test
  public void replayWorkflowExecutionWithVersionAndNewActivity() throws Exception {

    final String workflowId = executeWorkflow(WorkflowCode.MyWorkflowImpl.class);

    final String eventHistory = getWorkflowExecutionHistory(workflowId).toJson(true);

    WorkflowReplayer.replayWorkflowExecution(
        eventHistory, MyWorkflowImplWithVersionAndNewActivity.class);
  }

  private String executeWorkflow(
      Class<? extends WorkflowCode.MyWorkflow> workflowImplementationType) {

    testWorkflowRule
        .getWorker()
        .registerActivitiesImplementations(new WorkflowCode.GreetingActivitiesImpl());

    testWorkflowRule.getWorker().registerWorkflowImplementationTypes(workflowImplementationType);

    testWorkflowRule.getTestEnvironment().start();

    WorkflowCode.MyWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                WorkflowCode.MyWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
    WorkflowExecution execution = WorkflowStub.fromTyped(workflow).start("Hello");
    // wait until workflow completes
    WorkflowStub.fromTyped(workflow).getResult(String.class);

    return execution.getWorkflowId();
  }

  public static class WorkflowImplWithTimerNotVersioned implements WorkflowCode.MyWorkflow {

    @Override
    public String getGreeting(String name) {
      Workflow.sleep(100);
      return createActivityStub().composeGreeting("Hello", name);
    }
  }

  public static class MyWorkflowImplWithVersionOnly implements WorkflowCode.MyWorkflow {

    @Override
    public String getGreeting(String name) {
      int version = Workflow.getVersion("my-version", Workflow.DEFAULT_VERSION, 1);
      if (version == 1) {
        // do nothing yet
      }
      return createActivityStub().composeGreeting("Hello", name);
    }
  }

  public static class MyWorkflowImplWithVersionAndNewActivity implements WorkflowCode.MyWorkflow {

    @Override
    public String getGreeting(String name) {
      int version = Workflow.getVersion("my-version", Workflow.DEFAULT_VERSION, 1);

      if (version == 1) {
        createActivityStub().composeGreeting("Hello", name);
      }

      return createActivityStub().composeGreeting("Hello", name);
    }
  }

  private WorkflowExecutionHistory getWorkflowExecutionHistory(final String workflowId) {
    return new WorkflowExecutionHistory(
        testWorkflowRule.getHistory(
            WorkflowExecution.newBuilder().setWorkflowId(workflowId).build()));
  }
}
