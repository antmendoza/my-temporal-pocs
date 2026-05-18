package com.antmendoza.temporal;

import io.temporal.activity.ActivityExecutionContext;
import io.temporal.common.interceptors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleActivityInterceptor extends WorkerInterceptorBase implements WorkerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SimpleActivityInterceptor.class);

    @Override
    public ActivityInboundCallsInterceptor interceptActivity(ActivityInboundCallsInterceptor next) {
        return new SimpleActivityInboundCallsInterceptor(next);
    }

    @Override
    public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {
        return next;
    }

    private static class SimpleActivityInboundCallsInterceptor
            extends ActivityInboundCallsInterceptorBase {

        public SimpleActivityInboundCallsInterceptor(ActivityInboundCallsInterceptor next) {
            super(next);
        }

        @Override
        public void init(ActivityExecutionContext context) {
            super.init(context);
        }

        @Override
        public ActivityOutput execute(ActivityInput input) {
            ActivityOutput output = super.execute(input);

            if(!(output.getResult() instanceof MyActivityResult)){
                return output;
            }


            boolean businessLoggingActivitySuccess = true;
            try {

                // Direct Java call: bypasses the activity stub, so this does NOT
                // schedule a Temporal activity (no history events, no retries, no
                // timeouts). Runs in the worker thread of the activity being
                // intercepted
                new GreetingActivitiesImpl().auditLogging();

            } catch (Exception e) {
                log.error("[error] <<< ", e);
                businessLoggingActivitySuccess = false;
            }

            return new ActivityOutput(new MyActivityResult(businessLoggingActivitySuccess,
                    ((MyActivityResult) output.getResult()).getActivityResult()));

        }
    }
}
