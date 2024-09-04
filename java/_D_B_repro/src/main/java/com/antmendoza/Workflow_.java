package com.antmendoza;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.TemporalFailure;
import io.temporal.workflow.*;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Workflow_ {

    @WorkflowInterface
    public interface ParentWorkflow {

        @WorkflowMethod
        String start(final WorkflowInput workflowInput);
    }

    @ActivityInterface
    public interface MyActivities {

        @ActivityMethod
        String completeAfter1Second();

        @ActivityMethod
        String failWithNonRetryableAfter3Seconds();

        @ActivityMethod
        String completeAfter6Seconds();
    }

    public static class ParentWorkflowImpl implements ParentWorkflow {

        private final MyActivities activities =
                Workflow.newActivityStub(
                        MyActivities.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(20)).build());

        private final Logger logger = Workflow.getLogger(ParentWorkflowImpl.class);

        @Override
        public String start(final WorkflowInput workflowInput) {

            List<Promise<String>> promises = new ArrayList<>();

            // Simulate some work
            Workflow.sleep(Duration.ofSeconds(3));

            promises.add(Async.function(this::successful_branch1));
            promises.add(Async.function(this::failed_branch2));
            promises.add(Async.function(this::delayed_branch3));

            try {
                Promise.allOf(promises).get();
            } catch (TemporalFailure e) {
                //                for (Promise<String> promise : promises) {
                //                    if (promise.getFailure() != null) {
                //                        promise.get();
                //                    }
                //                }
                throw e;
            }

            return "hello";
        }

        private String delayed_branch3() {
            return activities.completeAfter1Second();
        }

        private String failed_branch2() {
            return activities.failWithNonRetryableAfter3Seconds();
        }

        private String successful_branch1() {
            return activities.completeAfter6Seconds();
        }
    }

    /**
     * Simple activity implementation, that concatenates two strings.
     */
    static class MyActivitiesImpl implements MyActivities {

        private static void sleep(final int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String completeAfter1Second() {
            sleep(1_000);

            return null;
        }

        @Override
        public String failWithNonRetryableAfter3Seconds() {
            sleep(3_000);

            final boolean failActivity = Boolean.getBoolean("failBranch2");
            if (failActivity) {
                throw ApplicationFailure.newNonRetryableFailure("Myerror", "Myerror");
            }

            return null;
        }

        @Override
        public String completeAfter6Seconds() {
            sleep(6_000);
            return null;
        }
    }
}
