package com.antmendoza;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class MyWorkflowImpl implements MyWorkflow {

    private final GreetingActivities activities =
            Workflow.newActivityStub(
                    GreetingActivities.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String getGreeting(String name) {




        Workflow.sleep(Duration.ofSeconds(2));


        int a = Workflow.getVersion("v2", 2, 3);

        System.out.println(">>>>> " + a);

//        Workflow.getVersion("v3", 2, 3);

        Workflow.sleep(Duration.ofSeconds(20));

//
        final String hello = activities.composeGreeting("Hello", name);

        return hello;
    }

    @Override
    public String getGreetingQuery(String name) {
        return "";
    }
}
