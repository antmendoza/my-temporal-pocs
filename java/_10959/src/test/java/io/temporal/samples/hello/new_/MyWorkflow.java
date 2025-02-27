package io.temporal.samples.hello.new_;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyWorkflow<T> {

    @WorkflowMethod
    T getGreeting(String name);
}
