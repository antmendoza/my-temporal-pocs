package io.antmendoza.samples;

import io.antmendoza.samples.TestUtilInterceptorTracker;
import io.antmendoza.samples._20231006.TestUtilWorkflowInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkerInterceptorBase;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;

public class TestUtilWorkerInterceptor extends WorkerInterceptorBase {

  private final TestUtilInterceptorTracker testUtilInterceptorTracker;

  public TestUtilWorkerInterceptor(TestUtilInterceptorTracker testUtilInterceptorTracker) {
    this.testUtilInterceptorTracker = testUtilInterceptorTracker;
  }

  @Override
  public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {
    return new TestUtilWorkflowInboundCallsInterceptor(next, testUtilInterceptorTracker);
  }
}
