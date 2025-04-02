package io.temporal.samples.hello;

import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface GreetingWorkflow {


    @WorkflowMethod
    String mainMethod(String name);


    @UpdateMethod
    String updateMethod(String name);

    @UpdateMethod
    void exit(String name);
}
