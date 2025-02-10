package com.temporal.demos.temporalspringbootdemo.workflows;

import com.temporal.demos.temporalspringbootdemo.workflows.activity.MyActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@WorkflowImpl(taskQueues = "HelloSampleTaskQueue")
public class DemoWorkflowImpl implements DemoWorkflow {


    @Override
    public String exec(final String name) {
        MyActivity activity = Workflow.newActivityStub(MyActivity.class, ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(2)).build());


        return activity.doSomething();


    }
}
