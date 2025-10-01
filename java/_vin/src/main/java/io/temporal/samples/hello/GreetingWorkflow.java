package io.temporal.samples.hello;

import io.temporal.workflow.*;

@WorkflowInterface
public interface GreetingWorkflow {


    @WorkflowMethod
    String mainMethod(String name);


    @UpdateMethod
    String getNotification(String notification);

}
