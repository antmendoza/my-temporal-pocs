package io.temporal.samples;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities activities =
            Workflow.newActivityStub(
                    GreetingActivities.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(5))
                            .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(5).build())
                            .build());

    @Override
    public String getGreeting(String name) {

        activities.activity_1(new MyActivityInput(true,"activityInput1"));

        activities.activity_2(new MyActivityInput(false,"activityInput2"));

        activities.activity_3(new MyActivityInput(true,"activityInput3"));

        return "done";


    }
}
