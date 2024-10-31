package com.antmendoza;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyChildWorkflow {

  @WorkflowMethod
  String getGreeting(String name);

  @SignalMethod
  void signalHandler(String value);
}
