package com.antmendoza;

import io.temporal.activity.*;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HelloActivity {


    @WorkflowInterface
    public interface GreetingWorkflow {

        @WorkflowMethod
        String start();

        @UpdateMethod
        void update(String signal_1);
    }

    @ActivityInterface
    public interface GreetingActivities {

        String activity1(String name);

    }


    public static class GreetingWorkflowImpl implements GreetingWorkflow {

        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());


        private final List<String> pendingUpdates = new ArrayList<>();
        private final List<String> completedUpdates = new ArrayList<>();
        private final List<String> result = new ArrayList<>();


        @Override
        public String start() {

            boolean exitWhile = false;

            while (!exitWhile) {

                Workflow.await(() -> !pendingUpdates.isEmpty());


                final String updateData = pendingUpdates.remove(0);
                final String hello = activities.activity1(updateData);

                completedUpdates.add(updateData);


                result.add(hello);

                if (result.size() == 2) {
                    exitWhile = true;
                }
            }

            return result.stream().collect(Collectors.joining(","));
        }

        @Override
        public void update(final String signal) {

            pendingUpdates.add(signal);

            Workflow.await(() -> completedUpdates.contains(signal));

        }
    }

    static class GreetingActivitiesImpl implements GreetingActivities {
        public GreetingActivitiesImpl() {
        }

        @Override
        public String activity1(String name) {
            return "[" + name + "]";
        }

    }
}
