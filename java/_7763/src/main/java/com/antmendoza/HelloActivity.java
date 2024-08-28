package com.antmendoza;

import io.temporal.activity.*;
import io.temporal.workflow.*;

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

        @QueryMethod
        List<String> processedSignals();
    }

    @ActivityInterface
    public interface GreetingActivities {

        String activity1(String name);

    }


    public static class GreetingWorkflowImpl implements GreetingWorkflow {

        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder()
                                .setStartToCloseTimeout(Duration.ofSeconds(5)).build());


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


            if (signal.equals("SIGNAL_2") &&
                    //SIGNAL 1 NOT PROCESSED YET
                    !completedUpdates.contains("SIGNAL_1")) {
                return;
            }


            pendingUpdates.add(signal);

            Workflow.await(() -> completedUpdates.contains(signal));

        }

        @Override
        public List<String> processedSignals() {
            return completedUpdates;
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
