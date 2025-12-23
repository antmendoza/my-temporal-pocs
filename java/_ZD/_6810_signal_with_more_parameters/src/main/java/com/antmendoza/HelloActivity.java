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
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloActivity {

    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    static final String WORKFLOW_ID = "HelloActivityWorkflow";

    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);


        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);


        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());


        factory.start();

        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                .build());


        WorkflowExecution workflowStarted = WorkflowClient.start(workflow::getGreeting, "");


        client.newUntypedWorkflowStub(WORKFLOW_ID).signal("signal", "signalWithCorrectParameters", true);
        client.newUntypedWorkflowStub(WORKFLOW_ID).signal("signal", "signalWithMoreParameters1AsFirstValue", 1, 5);
        client.newUntypedWorkflowStub(WORKFLOW_ID).signal("signal", "signalWithMoreParameters0AsFirstValue", 0, 5);
        client.newUntypedWorkflowStub(WORKFLOW_ID).signal("newSignalObject", new MySignalRequest("newSignalObject", 0,9));



        System.out.println("querySignalMap " + workflow.querySignalMap());

        String greeting = workflow.getGreeting("World");

        // Display workflow execution results
        System.out.println(greeting);
        System.exit(0);
    }

    @WorkflowInterface
    public interface GreetingWorkflow {

        @WorkflowMethod
        String getGreeting(String name);


        @SignalMethod
        void signal(String signal, Boolean signalSpecificFlag);


        @SignalMethod
        void newSignal(String signal, Integer firstCounter, Integer secondCounter);


        @SignalMethod
        void newSignalObject(MySignalRequest mySignalRequest);



        @QueryMethod
        Map<String , Boolean> querySignalMap();
    }

    @ActivityInterface
    public interface GreetingActivities {


        @ActivityMethod(name = "greet")
        String composeGreeting(String greeting, String name);
    }

    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowImpl implements GreetingWorkflow {
        Map<String, Boolean> signalMap = new HashMap<>();


        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

        @Override
        public String getGreeting(String name) {


            Workflow.sleep(2000);
            return activities.composeGreeting("Hello", name);
        }

        @Override
        public void signal(final String signal, final Boolean signalSpecificFlag) {
            this.signalMap.put(signal, signalSpecificFlag);
        }

        @Override
        public void newSignal(final String signal, final Integer firstCounter, final Integer secondCounter) {

        }

        @Override
        public void newSignalObject(final MySignalRequest mySignalRequest) {
            this.signal(mySignalRequest.getSignal(), mySignalRequest.getSignalSpecificFlag());
        }

        @Override
        public Map<String, Boolean> querySignalMap() {
            return this.signalMap;
        }


    }

    static class GreetingActivitiesImpl implements GreetingActivities {
        private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

        @Override
        public String composeGreeting(String greeting, String name) {
            log.info("Composing greeting...");
            return greeting + " " + name + "!";
        }
    }
}
