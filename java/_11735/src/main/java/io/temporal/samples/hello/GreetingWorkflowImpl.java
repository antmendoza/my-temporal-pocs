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



        if(WorkflowUnsafe.isReplaying()){
            System.out.println("Replaying workflow...");
        }

        int versionMyChange = Workflow.getVersion("versionMyChange", Workflow.DEFAULT_VERSION, 1);

        while (!exit || !list.isEmpty()) {
            Workflow.await(() -> !list.isEmpty());
            String fromList = list.get(0);

            if (versionMyChange == 1){
                activities.sleep5Seconds("mainMethod", fromList);
            }

            list.remove(fromList);


        }

        return "hello";
    }

    @Override
    public String getNotification(final String name) {

        String result;
            list.add(name);
        try {
            lock.lock();
            result = activities.sleep5Seconds("updateMethod", name);
        } finally {
            lock.unlock();
        }
        return result;

    }

    @Override
    public void aggregateNotification(final String name) {
        this.exit = true;
    }

    @Override
    public String my_query() {
        return "random query";
    }
}
