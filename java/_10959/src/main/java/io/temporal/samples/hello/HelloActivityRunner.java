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

import io.temporal.activity.ActivityOptions;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.payload.codec.ZlibPayloadCodec;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.time.Duration;


public class HelloActivityRunner {

  static final String TASK_QUEUE = "HelloActivityTaskQueue";

  static final String WORKFLOW_ID = "HelloActivityWorkflow";


  @WorkflowInterface
  public interface GreetingWorkflow {


    @WorkflowMethod
    String getGreeting(String name);
  }

  // Define the workflow implementation which implements our getGreeting workflow method.
  public static class GreetingWorkflowImpl implements GreetingWorkflow {


    private final TestActivity<MyRequest, MyRequest> activities =
        Workflow.newActivityStub(
                TestActivity.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String getGreeting(String name) {
      // This is a blocking call that returns only after the activity has completed.
      final MyRequest myRequest = activities.activity1(new MyRequest(name, "1"));
      return myRequest.getName();
    }
  }



  public static void main(String[] args) {

    // Get a Workflow service stub.
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();


    final DefaultDataConverter dataConverter = DefaultDataConverter.newDefaultInstance().withPayloadConverterOverrides(new MyPayloadConverter());
    WorkflowClient client = WorkflowClient.newInstance(
            service,
            WorkflowClientOptions.newBuilder()
                    .setDataConverter(dataConverter)
                    .build());

    final WorkerFactory factory = WorkerFactory.newInstance(client);


    final Worker worker = factory.newWorker(TASK_QUEUE,
            WorkerOptions.newBuilder().build());


    worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
    worker.registerActivitiesImplementations(new ActivityImpl());

    factory.start();

    GreetingWorkflow workflow =
        client.newWorkflowStub(
            GreetingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                    .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_TERMINATE_IF_RUNNING)
                .setTaskQueue(TASK_QUEUE)
                .build());


    String greeting = workflow.getGreeting("World");

    // Display workflow execution results
    System.out.println(greeting);
    System.exit(0);
  }
}
