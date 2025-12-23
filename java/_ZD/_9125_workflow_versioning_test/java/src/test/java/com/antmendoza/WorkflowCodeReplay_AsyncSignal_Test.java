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

import static com.antmendoza.WorkflowCode_AyncSignal.TASK_QUEUE;
import static com.antmendoza.WorkflowCode_AyncSignal.createAsyncChildWorkflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.common.WorkflowExecutionHistory;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.testing.WorkflowReplayer;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

public class WorkflowCodeReplay_AsyncSignal_Test {

  @Rule
  public TestWorkflowRule testWorkflowRule =
      TestWorkflowRule.newBuilder()
          //.setNamespace("default")
          //.setUseExternalService(true)
          .setDoNotStart(true)
          .build();

  @AfterEach
  public void after() {
    testWorkflowRule.getTestEnvironment().shutdown();
  }

  @Test
  public void replayWorkflowExecution() throws Exception {

    final Class<WorkflowCode_AyncSignal.MyWorkflowJPMCCodeImpl> workflowImplementationType =
        WorkflowCode_AyncSignal.MyWorkflowJPMCCodeImpl.class;

    createWorker(WorkflowCode_AyncSignal.MyWorkflowJPMCCodeImpl.class);
    final String workflowId = executeMainWorkflow();

    final String eventHistory = getWorkflowExecutionHistory(workflowId).toJson(true);

    WorkflowReplayer.replayWorkflowExecution(eventHistory, workflowImplementationType);
  }

  @Test
  public void replayWorkflowExecutionWithVersionAndGetChild() throws Exception {

    final Class<WorkflowCode_AyncSignal.MyWorkflowJPMCCodeImpl> workflowImplementationType =
        WorkflowCode_AyncSignal.MyWorkflowJPMCCodeImpl.class;

    createWorker(WorkflowCode_AyncSignal.MyWorkflowJPMCCodeImpl.class);
    final String workflowId = executeMainWorkflow();

    final String eventHistory = getWorkflowExecutionHistory(workflowId).toJson(true);

    WorkflowReplayer.replayWorkflowExecution(
        eventHistory, MyWorkflowImplWithGetChildPromiseVersioned.class);
  }

  private String executeMainWorkflow() {

    WorkflowCode_AyncSignal.MyWorkflowJPMCCode workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                WorkflowCode_AyncSignal.MyWorkflowJPMCCode.class,
                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
    WorkflowExecution execution = WorkflowStub.fromTyped(workflow).start("World");
    // wait until workflow completes
    String result = WorkflowStub.fromTyped(workflow).getResult(String.class);

    Assert.assertEquals("Hello World!", result);

    return execution.getWorkflowId();
  }

  private void createWorker(final Class<?> workflowImplementationType) {
    testWorkflowRule.getWorker().registerActivitiesImplementations(new GreetingActivitiesImpl());

    testWorkflowRule
        .getWorker()
        .registerWorkflowImplementationTypes(workflowImplementationType, MyChildWorkflowImpl.class);

    testWorkflowRule.getTestEnvironment().start();
  }

  private WorkflowExecutionHistory getWorkflowExecutionHistory(final String workflowId) {
    return new WorkflowExecutionHistory(
        testWorkflowRule.getHistory(
            WorkflowExecution.newBuilder().setWorkflowId(workflowId).build()));
  }

  public static class MyWorkflowImplWithGetChildPromiseVersioned
      implements WorkflowCode_AyncSignal.MyWorkflowJPMCCode {

    private final GreetingActivities activities =
        Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String getGreeting(String name) {

      final String childWorkflow1 = "child_workflow_1_" + Workflow.currentTimeMillis();

      final WorkflowCode_AyncSignal.MyChildWorkflow child_1 = createAsyncChildWorkflow(name, childWorkflow1, TASK_QUEUE);
      // Wait for child to start
      Workflow.getWorkflowExecution(child_1).get();

      //      1	call an activity
      // This is a blocking call that returns only after the activity has completed.
      final String hello = activities.composeGreeting("Hello", name);

      //      2	signal external workflow

      Promise signalWorkflow =
              Async.procedure(
                      WorkflowCode_AyncSignal.MyWorkflowJPMCCodeImpl::signalWorkflow, childWorkflow1);
      // signalWorkflow.get();

      //      Workflow.newUntypedExternalWorkflowStub(childWorkflow1).signal("signal_1", "value_1");

      //      3	start child workflow using Async.function
      final String childWorkflow2 = "child_workflow_2_";
      final WorkflowCode_AyncSignal.MyChildWorkflow child_2 =
              createAsyncChildWorkflow(name, childWorkflow2, TASK_QUEUE);

      //      4	use getVersion
      int version = Workflow.getVersion("get-child-workflow", Workflow.DEFAULT_VERSION, 1);

      if (version == 1) {

        //      5	query execution details of the started child workflow in step 3 if version == 1
        // (i.e. not default version)
       // Workflow.getWorkflowExecution(child_2).get();
      }

      Workflow.sleep(Duration.ofSeconds(2));

      return hello;
    }
  }
}
