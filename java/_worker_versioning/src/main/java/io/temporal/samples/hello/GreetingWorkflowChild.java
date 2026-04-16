package io.temporal.samples.hello;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface GreetingWorkflowChild {


    @WorkflowMethod
    String mainMethod(String name, Integer iterations);



    @QueryMethod
    default String getGreeting(){
        return Math.random() +"";
    }
}
