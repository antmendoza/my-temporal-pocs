package io.temporal.samples.tracing.workflow;

import io.temporal.workflow.*;

@WorkflowInterface
public interface TracingWorkflow {

  @WorkflowMethod
  String greet(String name);

  @SignalMethod
  void setLanguage(String language);

  @UpdateMethod
  void updateMethod(String language);

  @QueryMethod
  String getLanguage();
}
