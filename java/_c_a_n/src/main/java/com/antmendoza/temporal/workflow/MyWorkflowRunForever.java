package com.antmendoza.temporal.workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyWorkflowRunForever {

    @WorkflowMethod
    String run(String name);


    @QueryMethod
    String status();
}
