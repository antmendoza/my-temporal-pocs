package com.antmendoza;

public class MyWorkflowImpl implements MyWorkflow {
  @Override
  public String execute() {
    return "done";
  }

  @Override
  public String myquery() {
    return "my_result";
  }
}
