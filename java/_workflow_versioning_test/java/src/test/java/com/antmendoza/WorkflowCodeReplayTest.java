package com.antmendoza;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.common.WorkflowExecutionHistory;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.testing.WorkflowReplayer;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

public class WorkflowCodeReplayTest {

    @Rule
    public TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder()
                    // .setNamespace("default")
                    // .setUseExternalService(true)
                    .setDoNotStart(true)
                    .build();

    @AfterEach
    public void after() {
        testWorkflowRule.getTestEnvironment().shutdown();
    }

    @Test
    public void replayWorkflowExecution() throws Exception {

        final Class<MyWorkflowImpl> workflowImplementationType =
                MyWorkflowImpl.class;

        createWorker(MyWorkflowImpl.class);
        final String workflowId = executeMainWorkflow();

        testWorkflowRule.getTestEnvironment().shutdownNow();

        createWorker(MyWorkflowImpl.class);

        final String workflowId2 = executeMainWorkflow();


        final String eventHistory = getWorkflowExecutionHistory(workflowId2).toJson(true);

        WorkflowReplayer.replayWorkflowExecution(eventHistory, workflowImplementationType);
    }


    private String executeMainWorkflow() {

        MyWorkflow workflow =
                testWorkflowRule
                        .getWorkflowClient()
                        .newWorkflowStub(
                                MyWorkflow.class,
                                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
        WorkflowExecution execution = WorkflowStub.fromTyped(workflow).start("World");
        // wait until workflow completes
        String result = WorkflowStub.fromTyped(workflow).getResult(String.class);

        Assert.assertEquals("Hello World!", result);

        return execution.getWorkflowId();
    }

    private void createWorker(
            final Class<? extends MyWorkflow> workflowImplementationType) {
        testWorkflowRule.getWorker().registerActivitiesImplementations(new GreetingActivitiesImpl());

        testWorkflowRule
                .getWorker()
                .registerWorkflowImplementationTypes(workflowImplementationType);

        testWorkflowRule.getTestEnvironment().start();
    }

    private WorkflowExecutionHistory getWorkflowExecutionHistory(final String workflowId) {
        return new WorkflowExecutionHistory(
                testWorkflowRule.getHistory(
                        WorkflowExecution.newBuilder().setWorkflowId(workflowId).build()));
    }


}
