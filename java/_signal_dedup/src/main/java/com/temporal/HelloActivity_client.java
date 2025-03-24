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

package com.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.workflow.*;

/** Sample Temporal Workflow Definition that executes a single Activity. */
public class HelloActivity_client {

  // Define the task queue name
  static final String TASK_QUEUE = "HelloActivityTaskQueue";

  // Define our workflow unique id
  static final String WORKFLOW_ID = "HelloActivityWorkflow";

  /**
   * With our Workflow and Activities defined, we can now start execution. The main method starts
   * the worker and then the workflow.
   */
  public static void main(String[] args) {

    // Get a Workflow service stub.
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

    /*
     * Get a Workflow service client which can be used to start, Signal, and Query Workflow Executions.
     */
    WorkflowClient client = WorkflowClient.newInstance(service);


    sendSignal(client, "key", "value that should be printed, check worker logs");

    wait_for(6000);

    sendSignal(client, "key", "value this shouldn't be printed");
    wait_for(3000);
    sendSignal(client, "key2", "value this shouldn't be printed");
    sendSignal(client, "key", "value_2 that should be printed, check worker logs");
    wait_for(3000);
    sendSignal(client, "key2", "value_2 that should be printed, check worker logs");
  }

  private static void sendSignal(final WorkflowClient client, final String key, final String msg) {

    System.out.println("Sending signal: " + msg);

    client.newUntypedWorkflowStub("HelloActivityWorkflow").signal("mySignal", key, msg);
  }

  private static void wait_for(final int wait) {
    try {
      Thread.sleep(wait);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
