package io.temporal.samples.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowLock;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GreetingWorkflowImpl implements GreetingWorkflow {


    final List<String> list = new ArrayList<>();
    private final GreetingActivities activities =
            Workflow.newActivityStub(
                    GreetingActivities.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());
    WorkflowLock lock = Workflow.newWorkflowLock();
    private boolean exit = false;

    @Override
    public String mainMethod(String name) {

        final List<Promise<String>> promises =  new ArrayList<>();
        for (int b = 0; b < 3; b++) {
            for (int i = 0; i < 3; i++) {
                promises.add(Async.function(activities::doSomething, "a"));
            }
            Promise.allOf(promises).get();
        }

        return "hello";
    }


}
