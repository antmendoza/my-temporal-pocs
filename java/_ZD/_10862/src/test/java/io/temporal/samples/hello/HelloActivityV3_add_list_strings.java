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

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;


public class HelloActivityV3_add_list_strings {


    @WorkflowInterface
    public interface GreetingWorkflow {


        @WorkflowMethod
        String execute(String name, List<String> names);


        @SignalMethod
        void signal(String name, List<String> names);


    }


    public static class GreetingWorkflowImpl implements GreetingWorkflow {


        private boolean signaled = false;

        @Override
        public String execute(String name, List<String> names) {

            Workflow.await(() -> this.signaled);

            return "done";
        }

        @Override
        public void signal(final String name, List<String> names) {
            System.out.println("Signal received");
            this.signaled = true;
        }
    }


}
