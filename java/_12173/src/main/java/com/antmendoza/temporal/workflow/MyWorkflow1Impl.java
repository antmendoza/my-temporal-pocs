package com.antmendoza.temporal.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MyWorkflow1Impl implements MyWorkflow1 {

    MyActivity myActivity = Workflow.newActivityStub(MyActivity.class, ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(2))
            .build());

    @Override
    public String run(int numCAN, int currentIteration) {

        final List<Promise<String>> promises = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            promises.add(Async.function(myActivity::doSomething, "input-" + i + "-" + currentIteration));
        }
        Promise.allOf(promises).get();

        if (currentIteration <= numCAN) {
            Workflow.continueAsNew(numCAN, currentIteration + 1);
        }


        return "done";
    }

}
