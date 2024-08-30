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

        String simulateDelayToInitWorkflow(String name);
        String activity(String name);

    }


    public static class GreetingWorkflowImpl implements GreetingWorkflow {

        public static final String SIGNAL_2 = "SIGNAL_2";
        public static final String SIGNAL_1 = "SIGNAL_1";
        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder()
                                .setStartToCloseTimeout(Duration.ofSeconds(5)).build());


        private final List<String> pendingUpdates = new ArrayList<>();
        private final List<String> completedUpdates = new ArrayList<>();
        private final List<String> result = new ArrayList<>();

        private String nextExpectedSignal = null;

        @Override
        public String start() {

            activities.simulateDelayToInitWorkflow("");

            nextExpectedSignal = SIGNAL_1;

            boolean exitWhile = false;
            while (!exitWhile) {
                Workflow.await(() -> !pendingUpdates.isEmpty());

                final String updateData = pendingUpdates.remove(0);
                final String hello = activities.activity(updateData);

                completedUpdates.add(updateData);

                result.add(hello);

                nextExpectedSignal =SIGNAL_2;

                if (result.size() == 2) {
                    exitWhile = true;
                }
            }

            return result.stream().collect(Collectors.joining(","));
        }

        @Override
        public void update(final String signal) {


            if(!signal.equals(nextExpectedSignal)){
                System.out.println("Skipping process signal=" +  signal);
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

}
