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
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class HelloActivity {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";


    @WorkflowInterface
    public interface GreetingWorkflow {


        @WorkflowMethod
        String getGreeting(String name);
    }


    @ActivityInterface
    public interface GreetingActivities {

        // Define your activity method which can be called during workflow execution
        @ActivityMethod
        String composeGreeting(String greeting, String name);
    }

    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowImpl implements GreetingWorkflow {


        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

        @Override
        public String getGreeting(String name) {

            //Workflow.sleep(Duration.ofSeconds(5));

            // This is a blocking call that returns only after the activity has completed.
            return activities.composeGreeting("Hello", name);
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
