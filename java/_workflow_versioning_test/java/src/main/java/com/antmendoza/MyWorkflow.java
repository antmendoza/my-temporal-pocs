package com.antmendoza;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyWorkflow {

    @WorkflowMethod
    String getGreeting(String name);
}
