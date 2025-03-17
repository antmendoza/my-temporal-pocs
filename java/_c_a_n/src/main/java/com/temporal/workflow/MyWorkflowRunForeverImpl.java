package com.temporal.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

import java.time.Duration;

public class MyWorkflowRunForeverImpl implements MyWorkflowRunForever {

    private final MyActivity activity = Workflow.newActivityStub(MyActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(5))
                    .build());



    @Override
    public String run(final String name) {

        if(WorkflowUnsafe.isReplaying()){
           // System.out.println(Workflow.getInfo().getWorkflowId() +  ":  Replaying workflow ");
        }


        while (true) {
            activity.doSomething(name);
        }


    }

    @Override
    public String status() {
        return "";
    }
}
