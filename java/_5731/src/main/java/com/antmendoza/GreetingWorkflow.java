package com.antmendoza;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;


@WorkflowInterface
public interface GreetingWorkflow {

    @WorkflowMethod
    String getGreeting();



    @ActivityInterface
    public interface GreetingActivities {

        @ActivityMethod
        String getTraceIdFromContext();
    }

    /**
     * Simple activity implementation, that concatenates two strings.
     */
    static class GreetingActivitiesImpl implements GreetingActivities {
        private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

        @Override
        public String getTraceIdFromContext() {
            final String traceId = MDC.get("traceId");
            log.info("Composing greeting...with traceId = " + traceId);
            return traceId;
        }
    }


    public class GreetingWorkflowImpl implements GreetingWorkflow {

        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

        @Override
        public String getGreeting() {

            String traceId = activities.getTraceIdFromContext();

            return traceId;
        }



    }

}

