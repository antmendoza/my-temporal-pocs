package io.temporal.poc.sample;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface GreetingChildWorkflow {

    @WorkflowMethod
    String emphasize(String greeting);
}