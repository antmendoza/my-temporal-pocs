package io.temporal.samples.hello;

import io.temporal.workflow.*;

@WorkflowInterface
public interface GreetingWorkflow {


    @WorkflowMethod
    String mainMethod(String name);


    @UpdateMethod
    String getNotification(String name);

    @SignalMethod
    void aggregateNotification(String name);


    @QueryMethod
    String my_query();
}
