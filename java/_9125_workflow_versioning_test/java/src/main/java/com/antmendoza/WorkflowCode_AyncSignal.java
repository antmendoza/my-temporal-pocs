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

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.ParentClosePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.*;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Sample Temporal Workflow Definition that executes a single Activity. */
public class WorkflowCode_AyncSignal {

  // Define the task queue name
  static final String TASK_QUEUE = "WorkflowCode_from_jpmc_code";

  // Define our workflow unique id
  static final String WORKFLOW_ID = "WorkflowCode_from_jpmc_code";

  /**
   * With our Workflow and Activities defined, we can now start execution. The main method starts
   * the worker and then the workflow.
   */
  public static void main(String[] args) {

    // Get a Workflow service stub.
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

    WorkflowClient client = WorkflowClient.newInstance(service);

    WorkerFactory factory = WorkerFactory.newInstance(client);

    Worker worker = factory.newWorker(TASK_QUEUE);

    worker.registerWorkflowImplementationTypes(
        MyWorkflowJPMCCodeImpl.class, MyChildWorkflowImpl.class);

    worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

    factory.start();

    // Create the workflow client stub. It is used to start our workflow execution.
    MyWorkflowJPMCCode workflow =
        client.newWorkflowStub(
            MyWorkflowJPMCCode.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .build());

    String greeting = workflow.getGreeting("World");

    // Display workflow execution results
    System.out.println(greeting);
    // System.exit(0);
  }

  @WorkflowInterface
  public interface MyWorkflowJPMCCode {

    @WorkflowMethod
    String getGreeting(String name);
  }

  @WorkflowInterface
  public interface MyChildWorkflow {

    @WorkflowMethod
    String getGreeting(String name);

    @SignalMethod
    void signalHandler();
  }

  public static class MyChildWorkflowImpl implements MyChildWorkflow {

    @Override
    public String getGreeting(final String name) {
      Workflow.sleep(Duration.ofSeconds(1));
      return "";
    }

    @Override
    public void signalHandler() {}
  }

  @ActivityInterface
  public interface GreetingActivities {

    // Define your activity method which can be called during workflow execution
    @ActivityMethod(name = "greet")
    String composeGreeting(String greeting, String name);
  }

  public static class MyWorkflowJPMCCodeImpl implements MyWorkflowJPMCCode {

    private final GreetingActivities activities =
        Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String getGreeting(String name) {

      final String childWorkflow1 = "child_workflow_1_" + Workflow.currentTimeMillis();

      final MyChildWorkflow child_1 = createAsyncChildWorkflow(name, childWorkflow1, TASK_QUEUE);
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
      final MyChildWorkflow child_2 = createAsyncChildWorkflow(name, childWorkflow2, TASK_QUEUE);

      //      Workflow.getVersion("ww", Workflow.DEFAULT_VERSION, 1);

      //      4	use getVersion

      //      5	query execution details of the started child workflow in step 3 if version == 1
      // (i.e. not default version)

      // Workflow.getWorkflowExecution(child_2).get();

      Workflow.sleep(Duration.ofSeconds(2));

      return hello;
    }

    public static void signalWorkflow(final String childWorkflow) {
      Workflow.newUntypedExternalWorkflowStub(childWorkflow).signal("signalHandler");
    }
  }

  static Promise<WorkflowExecution> createPromiseChildWorkflow(
      final String name, final String childWorkflowId) {

    final MyChildWorkflow child = createAsyncChildWorkflow(name, childWorkflowId, TASK_QUEUE);
    final Promise<WorkflowExecution> childExecution = Workflow.getWorkflowExecution(child);
    return childExecution;
  }

  static MyChildWorkflow createAsyncChildWorkflow(
      final String name, final String childWorkflowId, final String taskqueue_name) {
    final MyChildWorkflow child =
        Workflow.newChildWorkflowStub(
            MyChildWorkflow.class,
            ChildWorkflowOptions.newBuilder()
                .setWorkflowId(childWorkflowId)
                .setTaskQueue(taskqueue_name)
                .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_ABANDON)
                .build());

    Promise<String> promise =
        Async.function(child::getGreeting, name)
            .thenApply(
                (result) -> {
                  System.out.println("Hello " + result);
                  return result;
                });

    return child;
  }

  /** Simple activity implementation, that concatenates two strings. */
  static class GreetingActivitiesImpl implements GreetingActivities {
    private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

    @Override
    public String composeGreeting(String greeting, String name) {
      log.info("Composing greeting...");
      return greeting + " " + name + "!";
    }
  }
}
