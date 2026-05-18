package com.antmendoza.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.common.interceptors.*;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SimpleWorkflowInterceptor extends WorkerInterceptorBase {

    @Override
    public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {
        return new SimpleWorkflowInboundCallsInterceptor(next);
    }

    private static class SimpleWorkflowInboundCallsInterceptor
            extends WorkflowInboundCallsInterceptorBase {

        private SimpleWorkflowOutboundCallsInterceptor outbound;

        public SimpleWorkflowInboundCallsInterceptor(WorkflowInboundCallsInterceptor next) {
            super(next);
        }

        @Override
        public void init(WorkflowOutboundCallsInterceptor outboundCalls) {
            this.outbound = new SimpleWorkflowOutboundCallsInterceptor(outboundCalls);
            super.init(this.outbound);
        }

        @Override
        public WorkflowOutput execute(WorkflowInput input) {
            WorkflowOutput output = super.execute(input);
            if (!outbound.auditLoggingPromises.isEmpty()) {
                Promise.allOf(outbound.auditLoggingPromises).get();
            }
            return output;
        }
    }

    private static class SimpleWorkflowOutboundCallsInterceptor
            extends WorkflowOutboundCallsInterceptorBase {

        public SimpleWorkflowOutboundCallsInterceptor(WorkflowOutboundCallsInterceptor next) {
            super(next);
        }

        private final Logger log = Workflow.getLogger(SimpleWorkflowInterceptor.class);

        final List<Promise<Void>> auditLoggingPromises = new ArrayList<>();

        final GreetingActivities auditLoggingStub =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(5))
                                .setRetryOptions(
                                        RetryOptions.newBuilder()
                                                //.setMaximumAttempts(5)
                                                .setBackoffCoefficient(1.0)
                                                .build())
                                .build());

        @Override
        public <R> ActivityOutput<R> executeActivity(ActivityInput<R> input) {
            ActivityOutput<R> output = super.executeActivity(input);


            Object activityArg_0 = input.getArgs()[0];
            if (!(activityArg_0 instanceof MyActivityInput)) {
                return output;
            }

            if (!((MyActivityResult) output.getResult().get()).isAuditLoggingActivitySuccess()
                    //we could use context propagation to pass the information to the activity instead of the input
                    && ((MyActivityInput) activityArg_0).isTrackAuditLogging()) {

                log.info(
                        "[wf-interceptor] <<< activity '{}' auditLogging failed (activityId={}), scheduling activity auditLogging",
                        input.getActivityName(),
                        output.getActivityId());


                auditLoggingPromises.add(Async.procedure(auditLoggingStub::auditLogging));
            }


            return output;
        }
    }
}
