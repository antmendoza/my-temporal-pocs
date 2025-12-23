package com.temporal.demos.temporalspringbootdemo.workflows;

import io.cloudevents.CloudEvent;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DemoWorkflow {
    @WorkflowMethod
    String exec(String name);

}
