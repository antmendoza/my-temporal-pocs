package com.antmendoza;

import io.temporal.workflow.Workflow;
import java.time.Duration;

public class MyChildWorkflowImpl implements MyChildWorkflow {

  @Override
  public String getGreeting(final String name) {
    Workflow.sleep(Duration.ofSeconds(1));
    return "";
  }

  @Override
  public void signalHandler(final String value) {}
}
