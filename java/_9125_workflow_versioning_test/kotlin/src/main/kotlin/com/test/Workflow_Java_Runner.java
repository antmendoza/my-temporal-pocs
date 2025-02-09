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

package com.test;

import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.ParentClosePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.internal.sync.WorkflowInternal;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.*;

import java.time.Duration;

/** Sample Temporal Workflow Definition that executes a single Activity. */
public class Workflow_Java_Runner {



  // Define the task queue name
  static final String TASK_QUEUE = "Java-Workflow_task_queue";

  // Define our workflow unique id
  static final String WORKFLOW_ID = "Java-workflow";

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

    worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class, MyChildWorkflowImpl.class);

    worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

    factory.start();

    // Create the workflow client stub. It is used to start our workflow execution.
    MyWorkflow workflow =
        client.newWorkflowStub(
            MyWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .build());

    String greeting = workflow.getGreeting("World");

    // Display workflow execution results
    System.out.println("workflow completed with result " + greeting);


    try {
      workflow.query("");
    } catch (Exception e) {
      e.printStackTrace();

    }

    System.exit(0);

  }

  @WorkflowInterface
  public interface MyWorkflow {

    @WorkflowMethod
    String getGreeting(String name);

    @QueryMethod
    String  query(String s);
  }

  public static class MyWorkflowImpl implements MyWorkflow {

    private final GreetingActivities activities =
        Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String getGreeting(String name) {

      final String childWorkflow1 = "child_workflow_1_" + Workflow.currentTimeMillis();

      final MyChildWorkflow child_1 = createAsyncChildWorkflow(name, childWorkflow1);
      // Wait for child to start
      Workflow.getWorkflowExecution(child_1).get();

      //      1	call an activity
      // This is a blocking call that returns only after the activity has completed.
      final String hello = activities.composeGreeting("Hello", name);

      //      2	signal external workflow
      child_1.signalHandler(Math.random() + "");

      //      Workflow.newUntypedExternalWorkflowStub(childWorkflow1).signal("signal_1", "value_1");

      //      3	start child workflow using Async.function
      final String childWorkflow2 = "child_workflow_2_" + Workflow.currentTimeMillis();
      final MyChildWorkflow child_2 = createAsyncChildWorkflow(name, childWorkflow2);


      if(WorkflowInternal.isReplaying()){

        //      4	use getVersion
        Workflow.getVersion("get-child-workflow", Workflow.DEFAULT_VERSION, 1);

      }
      //      5	query execution details of the started child workflow in step 3 if version == 1
      // (i.e. not default version)

      //      6 signal external workflow W1 again
      //      6 signal external workflow W1 again
      child_1.signalHandler(Math.random() + "");

      return hello;
    }

    @Override
    public String query(final String s) {
      return "-";
    }
  }

  static Promise<WorkflowExecution> createPromiseChildWorkflow(
      final String name, final String childWorkflowId) {

    final MyChildWorkflow child = createAsyncChildWorkflow(name, childWorkflowId);
    final Promise<WorkflowExecution> childExecution = Workflow.getWorkflowExecution(child);
    return childExecution;
  }

  static MyChildWorkflow createAsyncChildWorkflow(final String name, final String childWorkflowId) {
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
}
