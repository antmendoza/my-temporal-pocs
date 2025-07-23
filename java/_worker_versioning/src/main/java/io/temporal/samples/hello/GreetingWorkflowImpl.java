package io.temporal.samples.hello;

import io.temporal.activity.ActivityOptions;
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


        for (int i = 0; i < 300; i++) {
            Workflow.sleep(Duration.ofSeconds(10));
            activities.sleep5Seconds("mainMethod", "fromList");

        }

        return "hello";
    }

}
