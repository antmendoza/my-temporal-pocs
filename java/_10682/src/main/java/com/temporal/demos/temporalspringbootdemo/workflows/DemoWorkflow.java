package com.temporal.demos.temporalspringbootdemo.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DemoWorkflow {
    @WorkflowMethod
    String exec(String name);

}
