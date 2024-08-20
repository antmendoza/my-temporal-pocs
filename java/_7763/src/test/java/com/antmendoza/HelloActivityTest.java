package com.antmendoza;

import static org.junit.Assert.assertEquals;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.*;
import org.junit.Rule;
import org.junit.Test;

public class HelloActivityTest {

    @Rule
    public TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder()
                    .setWorkflowTypes(HelloActivity.GreetingWorkflowImpl.class)
                    .setDoNotStart(true)
                    .build();

    @Test
    public void testExecuteTwoUpdates() {

        testWorkflowRule
                .getWorker()
                .registerActivitiesImplementations(
                        new HelloActivity.GreetingActivitiesImpl());
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

        workflow.update("SIGNAL_1");

        workflow.update("SIGNAL_2");

        String result = testWorkflowRule.getTestEnvironment()
                .getWorkflowClient()
                .newUntypedWorkflowStub(workflowId).getResult(String.class);
        assertEquals("[SIGNAL_1],[SIGNAL_2]", result);


        });



        testWorkflowRule.getTestEnvironment().shutdown();
    }


}
