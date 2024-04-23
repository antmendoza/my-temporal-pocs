package io.antmendoza.samples._5611;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.CompletablePromise;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class Workflow_5611Impl_CompletablePromiseAwait implements Workflow_5611 {

    private final Activity_5611 activity = Workflow.newActivityStub(Activity_5611.class,
            ActivityOptions.newBuilder().
                    setStartToCloseTimeout(Duration.ofSeconds(20))
                    .build()
    );

    @Override
    public String run() {

        CompletablePromise<Boolean> checkUpdate = Workflow.newPromise();

        boolean activityExecuted = Workflow.await(Duration.ofSeconds(5), () -> {
            return checkUpdate.get();
        });

        if (!activityExecuted) {
            System.out.println(">>>>>>>>>  timer fired");
        }

        return "done";
    }
}




