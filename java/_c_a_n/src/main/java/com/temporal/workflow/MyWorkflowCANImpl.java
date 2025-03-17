package com.temporal.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

import java.time.Duration;

public class MyWorkflowCANImpl implements MyWorkflowCAN {

    private final MyActivity activity = Workflow.newActivityStub(MyActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(5))
                    .build());


    @Override
    public String run(final String name) {

        if(WorkflowUnsafe.isReplaying()){
            // System.out.println(Workflow.getInfo().getWorkflowId() +  ":  Replaying workflow ");
        }

        //while (!Workflow.getInfo().isContinueAsNewSuggested()) {//isContinueAsNewSuggested 4k events or 4MB
        while(Workflow.getInfo().getHistoryLength() < 100){
            activity.doSomething(name);
        }

        Workflow.continueAsNew(name);

        return "";

    }

    @Override
    public String status() {
        return "";
    }
}
