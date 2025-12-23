package com.antmendoza;


import io.temporal.activity.Activity;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.*;
import io.temporal.worker.Worker;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static com.antmendoza.HelloActivity.GreetingWorkflowImpl.SIGNAL_1;
import static com.antmendoza.HelloActivity.GreetingWorkflowImpl.SIGNAL_2;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloActivityTest {


    @RegisterExtension
    public static final TestWorkflowExtension testWorkflowExtension =
            TestWorkflowExtension.newBuilder()
                    .setDoNotStart(true)
                    .build();



    @org.junit.jupiter.api.Test
    @Timeout(4)
    public void testExecuteTwoUpdates(TestWorkflowEnvironment testEnv) {

        final String taskQueue = "my-tq";
        final Worker worker = testEnv.newWorker(taskQueue);
        worker.registerWorkflowImplementationTypes(HelloActivity.GreetingWorkflowImpl.class);

        worker.registerActivitiesImplementations(
                new HelloActivity.GreetingActivities() {
                    @Override
                    public String simulateDelayToInitWorkflow(final String name) {
                        // simulate real world delay
                        sleep(500);
                        return null;
                    }

                    @Override
                    public String activity(final String name) {
                        recordSomething();

                        final int attempt = Activity.getExecutionContext().getInfo().getAttempt();
                        if (attempt == 1 && name.equals(SIGNAL_1)) {
                            throw new RuntimeException("exception from activity attempt=" + attempt);
                        }
                        return "[" + name + "]";
                    }

                    private void recordSomething() {
                        final int attempt = Activity.getExecutionContext().getInfo().getAttempt();
                        if (attempt == 1) {
                            System.out.println("recording something... ");
                        }
                    }
                });


        testEnv.start();

        // Get a workflow stub using the same task queue the worker uses.
        final String workflowId = "my-workflow";
        HelloActivity.GreetingWorkflow workflow =
                testEnv
                        .getWorkflowClient()
                        .newWorkflowStub(
                                HelloActivity.GreetingWorkflow.class,
                                WorkflowOptions.newBuilder()
                                        .setWorkflowId(workflowId)
                                        .setTaskQueue(taskQueue).build());


        WorkflowClient.start(workflow::start);

        waitWorkflowInitialized(testEnv, workflowId);

        workflow.update(SIGNAL_2);
        workflow.update(SIGNAL_1);
        assertEquals(List.of(SIGNAL_1), workflow.processedSignals());


        workflow.update(SIGNAL_2);
        assertEquals("[SIGNAL_1],[SIGNAL_2]", testEnv.getWorkflowClient()
                .newUntypedWorkflowStub(workflowId).getResult(String.class));

        testEnv.shutdown();


    }

    private static void waitWorkflowInitialized(final TestWorkflowEnvironment testEnv, final String workflowId) {
        boolean workflowInitialized = false;
        while (!workflowInitialized) {
            try {
                Thread.sleep(100);
                workflowInitialized = testEnv.getWorkflowClient().fetchHistory(workflowId)
                        .getHistory().getEventsList().stream().anyMatch(e -> {
                            return e.hasActivityTaskCompletedEventAttributes();
                        });

            } catch (Exception e) {
                //ignore
            }
        }
    }


    private static void sleep(final int sleepInMillis) {
        try {
            Thread.sleep(sleepInMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
