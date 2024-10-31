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

package com.antmendoza.version.order;

import com.antmendoza.GreetingActivities;
import com.antmendoza.GreetingActivitiesImpl;
import com.antmendoza.MyChildWorkflow;
import com.antmendoza.MyChildWorkflowImpl;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.ParentClosePolicy;
import io.temporal.api.history.v1.HistoryEvent;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.common.WorkflowExecutionHistory;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.worker.Worker;
import io.temporal.workflow.*;
import java.time.Duration;
import java.util.function.Predicate;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

public class WorkflowsWithVersionCheckEventOrderTest {

  @Rule
  public TestWorkflowRule testWorkflowRule =
      TestWorkflowRule.newBuilder()
          .setNamespace("default")
          .setUseExternalService(true)
          .setDoNotStart(true)
          .build();

  private static MyChildWorkflow createAsyncChildWorkflow_(
      final String name, final String childWorkflowId) {
    final MyChildWorkflow child =
        Workflow.newChildWorkflowStub(
            MyChildWorkflow.class,
            ChildWorkflowOptions.newBuilder()
                .setWorkflowId(childWorkflowId)
                .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_ABANDON)
                .build());

    Async.function(child::getGreeting, name);
    return child;
  }

  @AfterEach
  public void after() {
    testWorkflowRule.getTestEnvironment().shutdown();
  }

  @Test
  public void replayWorkflowExecution() throws Exception {
    createWorker();

    String resultimplementationMyWorkflowImplVersioned;
    String resultimplementationMyWorkflowImplWithoutVersion;
    final Predicate<HistoryEvent> historyEventPredicate =
        e -> {
          return e.hasSignalExternalWorkflowExecutionInitiatedEventAttributes()
              || e.hasStartChildWorkflowExecutionInitiatedEventAttributes();
        };

    {
      final Class<MyWorkflowWithoutVersionImpl> implementationMyWorkflowImplWithoutVersion =
          MyWorkflowWithoutVersionImpl.class;
      final String workflowId = executeMainWorkflow(MyWorkflowWithoutVersion.class);
      final WorkflowExecutionHistory eventHistory = getWorkflowExecutionHistory(workflowId);

      resultimplementationMyWorkflowImplWithoutVersion =
          eventHistory.getEvents().stream()
              .filter(historyEventPredicate)
              .map(event -> event.getEventType().toString())
              .toList()
              .toString();
    }
    {
      final Class<MyWorkflowVersionedImpl> implementationMyWorkflowImplVersioned =
          MyWorkflowVersionedImpl.class;
      final String workflowId = executeMainWorkflow(MyWorkflowVersioned.class);
      final WorkflowExecutionHistory eventHistory = getWorkflowExecutionHistory(workflowId);

      resultimplementationMyWorkflowImplVersioned =
          eventHistory.getEvents().stream()
              .filter(historyEventPredicate)
              .map(event -> event.getEventType().toString())
              .toList()
              .toString();
    }

    Assert.assertEquals(
        resultimplementationMyWorkflowImplVersioned,
        resultimplementationMyWorkflowImplWithoutVersion);

    //    WorkflowReplayer.replayWorkflowExecution(eventHistory,
    // implementationMyWorkflowImplWithoutVersion);
  }

  private String executeMainWorkflow(final Class<?> aClass) {

    Object workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                aClass,
                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
    WorkflowExecution execution = WorkflowStub.fromTyped(workflow).start("World");
    // wait until workflow completes
    String result = WorkflowStub.fromTyped(workflow).getResult(String.class);

    Assert.assertEquals("Hello World!", result);

    return execution.getWorkflowId();
  }

  private Worker createWorker() {
    final Worker worker = testWorkflowRule.getWorker();
    worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

    worker.registerWorkflowImplementationTypes(
        MyWorkflowVersionedImpl.class,
        MyWorkflowWithoutVersionImpl.class,
        MyChildWorkflowImpl.class);

    testWorkflowRule.getTestEnvironment().start();

    return worker;
  }

  private WorkflowExecutionHistory getWorkflowExecutionHistory(final String workflowId) {
    return new WorkflowExecutionHistory(
        testWorkflowRule.getHistory(
            WorkflowExecution.newBuilder().setWorkflowId(workflowId).build()));
  }

  @WorkflowInterface
  public interface MyWorkflowVersioned {

    @WorkflowMethod
    String getGreeting(String name);
  }

  @WorkflowInterface
  public interface MyWorkflowWithoutVersion {

    @WorkflowMethod
    String getGreeting(String name);
  }

  public static class MyWorkflowVersionedImpl implements MyWorkflowVersioned {

    private final GreetingActivities activities =
        Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String getGreeting(String name) {

      final String childWorkflow1 = "child_workflow_1_" + Workflow.currentTimeMillis();

      final MyChildWorkflow child_1 = createAsyncChildWorkflow_(name, childWorkflow1);
      // Wait for child to start
      Workflow.getWorkflowExecution(child_1).get();

      //      1	call an activity
      final String hello = activities.composeGreeting("Hello", name);

      //      2	signal external workflow
      child_1.signalHandler(Math.random() + "");

      //      3	start child workflow using Async.function
      final String childWorkflow2 = "child_workflow_2_" + Workflow.currentTimeMillis();
      final MyChildWorkflow child_2 = createAsyncChildWorkflow_(name, childWorkflow2);

      //      4	use getVersion
      int version = Workflow.getVersion("get-child-workflow", Workflow.DEFAULT_VERSION, 1);

      //      5	query execution details of the started child workflow in step 3 if version == 1
      // (i.e. not default version)
      if (version == 1) {
        // Wait for child to start
        Workflow.getWorkflowExecution(child_2).get();
      }

      //      6 signal external workflow W1 again
      child_1.signalHandler(Math.random() + "");

      return hello;
    }
  }

  public static class MyWorkflowWithoutVersionImpl implements MyWorkflowWithoutVersion {

    private final GreetingActivities activities =
        Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String getGreeting(String name) {

      final String childWorkflow1 = "child_workflow_1_" + Workflow.currentTimeMillis();

      final MyChildWorkflow child_1 = createAsyncChildWorkflow_(name, childWorkflow1);
      // Wait for child to start
      Workflow.getWorkflowExecution(child_1).get();

      //      1	call an activity
      final String hello = activities.composeGreeting("Hello", name);

      //      2	signal external workflow
      child_1.signalHandler(Math.random() + "");

      //      3	start child workflow using Async.function
      final String childWorkflow2 = "child_workflow_2_" + Workflow.currentTimeMillis();
      final MyChildWorkflow child_2 = createAsyncChildWorkflow_(name, childWorkflow2);

      //      4	use getVersion
      //      int version = Workflow.getVersion("get-child-workflow", Workflow.DEFAULT_VERSION, 1);

      //      5	query execution details of the started child workflow in step 3 if version == 1
      // (i.e. not default version)
      //     if (version == 1) {
      //        // Wait for child to start
      //        Workflow.getWorkflowExecution(child_2).get();
      //      }

      //      6 signal external workflow W1 again
      child_1.signalHandler(Math.random() + "");

      return hello;
    }
  }
}
