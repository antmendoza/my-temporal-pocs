package io.temporal.samples.hello;

import io.temporal.common.SearchAttributeKey;
import io.temporal.common.SearchAttributeUpdate;
import io.temporal.common.interceptors.WorkerInterceptorBase;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptorBase;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptorBase;

/**
 * Worker interceptor whose {@link WorkflowOutboundCallsInterceptor} issues a {@code getVersion} and
 * an {@code upsertTypedSearchAttributes} command on every activity invocation, before delegating to
 * the workflow's own outbound call.
 */
public class VersionSearchAttributeInterceptor extends WorkerInterceptorBase {

  static final SearchAttributeKey<String> INTERCEPTOR_SA =
      SearchAttributeKey.forKeyword("YourAttributeName");

  @Override
  public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {
    return new InboundCalls(next);
  }

  private static class InboundCalls extends WorkflowInboundCallsInterceptorBase {
    InboundCalls(WorkflowInboundCallsInterceptor next) {
      super(next);
    }

    @Override
    public void init(WorkflowOutboundCallsInterceptor outboundCalls) {
      super.init(new OutboundCalls(outboundCalls));
    }
  }

  private static class OutboundCalls extends WorkflowOutboundCallsInterceptorBase {
    OutboundCalls(WorkflowOutboundCallsInterceptor next) {
      super(next);
    }

    @Override
    public int getVersion(String changeId, int minSupported, int maxSupported) {

      int version = super.getVersion(changeId, minSupported, maxSupported);
      upsertTypedSearchAttributes(
              SearchAttributeUpdate.valueSet(
                      SearchAttributeKey.forKeyword("YourAttributeName")
                      , changeId));
      upsertTypedSearchAttributes(
              SearchAttributeUpdate.valueSet(
                      SearchAttributeKey.forKeyword("YourAttributeName_3")
                      , changeId ));

      return version;
    }

    @Override
    public <R> ActivityOutput<R> executeActivity(ActivityInput<R> input) {


      upsertTypedSearchAttributes(
              SearchAttributeUpdate.valueSet(
                      SearchAttributeKey.forKeyword("MostRecentStartedActivity")
                      , input.getActivityName() ));

      return super.executeActivity(input);
    }



  }
}