package com.antmendoza;

import static org.junit.Assert.assertEquals;

import io.temporal.activity.Activity;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.*;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HelloActivityTest {

    @Rule
    public TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder()
                    .setWorkflowTypes(HelloActivity.GreetingWorkflowImpl.class)
                    .setDoNotStart(true)
                    .build();

    private static void sleep(final int sleepInMillis) {
        try {
            Thread.sleep(sleepInMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testExecuteTwoUpdates() {

        testWorkflowRule
                .getWorker()
                .registerActivitiesImplementations(
                        new HelloActivity.GreetingActivities() {
                            @Override
                            public String activity1(final String name) {
                                // simulate real world delay
                                recordSomething();

                                final int attempt = Activity.getExecutionContext().getInfo().getAttempt();
                                if(attempt == 1){
                                    throw new RuntimeException("exception from activity attempt=" + attempt);
                                }
                                sleep(2000);
                                return "[" + name + "]";
                            }

                            private void recordSomething() {
                                final int attempt = Activity.getExecutionContext().getInfo().getAttempt();
                                if (attempt == 1) {
                                    System.out.println("recording something... ");
                                }
                            }
                        });


        testWorkflowRule.getTestEnvironment().start();

        // Get a workflow stub using the same task queue the worker uses.
        final String workflowId = "my-workflow";
        HelloActivity.GreetingWorkflow workflow =
                testWorkflowRule
                        .getWorkflowClient()
                        .newWorkflowStub(
                                HelloActivity.GreetingWorkflow.class,
                                WorkflowOptions.newBuilder()
                                        .setWorkflowId(workflowId)
                                        .setTaskQueue(testWorkflowRule.getTaskQueue()).build());

        WorkflowClient.start(workflow::start);


        CompletableFuture.runAsync(() -> {
            sleep(1000);
            //This update will be sent after SIGNAL_1 and while SIGNAL_1 is running
            workflow.update("SIGNAL_2");
        });

        workflow.update("SIGNAL_1");

        assertEquals(List.of("SIGNAL_1"), workflow.processedSignals());

        workflow.update("SIGNAL_2");


        String result = testWorkflowRule.getTestEnvironment()
                .getWorkflowClient()
                .newUntypedWorkflowStub(workflowId).getResult(String.class);
        assertEquals("[SIGNAL_1],[SIGNAL_2]", result);

        testWorkflowRule.getTestEnvironment().shutdown();


    }


}
