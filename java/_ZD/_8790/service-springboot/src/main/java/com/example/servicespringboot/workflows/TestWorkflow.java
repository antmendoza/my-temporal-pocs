package com.example.servicespringboot.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TestWorkflow {
    @WorkflowMethod
    String callTestActivity();
}
