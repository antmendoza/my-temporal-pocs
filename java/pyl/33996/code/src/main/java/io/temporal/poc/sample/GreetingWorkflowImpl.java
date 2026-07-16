package io.temporal.poc.sample;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities activities = Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(30))
                    .build());
    private Boolean signal;

    @Override
    public String greet(String name) {


        Workflow.sleep(Duration.ofSeconds(1));

        Workflow.await(() -> signal != null && signal);

        // one child workflow: post-process the greeting
        System.out.println("child workflow...");
        GreetingChildWorkflow child = Workflow.newChildWorkflowStub(GreetingChildWorkflow.class);
        return child.emphasize(name);
    }

    @Override
    public void signal(String name) {
        this.signal = true;
        Async.function(() -> activities.composeGreeting(name));

        System.out.println("signal...");

    }
}