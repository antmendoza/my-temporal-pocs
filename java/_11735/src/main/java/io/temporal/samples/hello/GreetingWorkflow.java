package io.temporal.samples.hello;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface GreetingWorkflow {


    @WorkflowMethod
    String getGreeting(String name);
}
