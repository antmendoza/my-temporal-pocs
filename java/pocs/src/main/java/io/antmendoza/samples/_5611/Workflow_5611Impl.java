package io.antmendoza.samples._5611;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class Workflow_5611Impl implements Workflow_5611 {

    private final Activity_5611 activity = Workflow.newActivityStub(Activity_5611.class,
            ActivityOptions.newBuilder().
                    setStartToCloseTimeout(Duration.ofSeconds(20))
                    .build()
    );


    @Override
    public String run() {


        boolean activityCompleted = Workflow.await(Duration.ofSeconds(5), () -> {
            System.out.println("In Workflow.await java");
            activity.doSomething();
            return true;
        });

        System.out.println("Completed " + !activityCompleted );

        if (!activityCompleted) {
            System.out.println(">>>>>>>>>  timer fired");
        }

        return "done";
    }
}




