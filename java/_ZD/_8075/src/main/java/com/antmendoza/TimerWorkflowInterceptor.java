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

import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptorBase;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;
import io.temporal.workflow.Async;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.Workflow;

public class TimerWorkflowInterceptor
        extends WorkflowInboundCallsInterceptorBase {


    public TimerWorkflowInterceptor(WorkflowInboundCallsInterceptor next) {
        super(next);
    }

    @Override
    public void init(WorkflowOutboundCallsInterceptor outboundCalls) {
        super.init(outboundCalls);
    }

    @Override
    public WorkflowOutput execute(WorkflowInput input) {

        //Send a notification every x seconds if the workflow hasn't completed after x time.
        CancellationScope timerScope = Workflow.newCancellationScope(() ->{
            Async.procedure(this::myFunction);

        });
        timerScope.run();
        final WorkflowOutput execute = super.execute(input);
        timerScope.cancel();
        return execute;

    }


    private void myFunction() {
        Workflow.sleep(1000);
        //Send notification, schedule activity, etc.
        System.out.println("print something with sleep");
        myFunction();
    }


}
