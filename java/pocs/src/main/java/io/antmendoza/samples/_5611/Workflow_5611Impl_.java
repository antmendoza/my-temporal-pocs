package io.antmendoza.samples._5611;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class Workflow_5611Impl_ implements Workflow_5611 {

    private final Activity_5611 activity = Workflow.newActivityStub(Activity_5611.class,
            ActivityOptions.newBuilder().
                    setStartToCloseTimeout(Duration.ofSeconds(20))
                    .build()
    );


    @Override
    public String run() {

        final AtomicBoolean activityCompleted = new AtomicBoolean(false);
        Async.procedure(activity::doSomething).thenApply((r) -> {
            activityCompleted.set(true);
            return r;
        });

        boolean activityExecuted = Workflow.await(Duration.ofSeconds(5), () -> {
            final boolean b = activityCompleted.get();
            activity.doSomething();
            return b;
        });

        if (!activityExecuted) {
            System.out.println(">>>>>>>>>  timer fired");
        }

        return "done";
    }
}




