package com.antmendoza;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListClosedWorkflowExecutionsRequest;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.List;

public class WorkflowCANTest {

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

        final Class<WorkflowCANImpl> workflowImplementationType =
                WorkflowCANImpl.class;


        testWorkflowRule
                .getWorker()
                .registerWorkflowImplementationTypes(WorkflowCANImpl.class);

        testWorkflowRule.getTestEnvironment().start();

        WorkflowCAN workflow1 =
                testWorkflowRule
                        .getWorkflowClient()
                        .newWorkflowStub(
                                WorkflowCAN.class,
                                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());


        WorkflowExecution execution1 = WorkflowStub.fromTyped(workflow1).start("World");

        // wait until workflow completes
        String result1 = WorkflowStub.fromTyped(workflow1).getResult(String.class);

        Assert.assertEquals("done", result1);


        List<WorkflowExecutionInfo> executionsList = testWorkflowRule.getWorkflowClient().getWorkflowServiceStubs().blockingStub()
                .listClosedWorkflowExecutions(ListClosedWorkflowExecutionsRequest.newBuilder()
                        .setNamespace(testWorkflowRule.getTestEnvironment().getNamespace()).build()).getExecutionsList();

        Assert.assertEquals(2, executionsList.toArray().length);


        Assert.assertEquals(1, executionsList.stream().filter(
                e -> e.getStatus() == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_CONTINUED_AS_NEW).count());


        Assert.assertEquals(1, executionsList.stream().filter(
                e -> e.getStatus() == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED).count());


        //TODO Challenge is how to mock Workflow.getInfo().iscontinuedAsNew() for testing purposes

    }


    @WorkflowInterface
    public interface WorkflowCAN {

        @WorkflowMethod
        public String greet(String name);

    }


    public static class WorkflowCANImpl implements WorkflowCAN {
        @Override
        public String greet(String name) {

            if (name != null
                    || Workflow.getInfo().isContinueAsNewSuggested()) {
                //TODO Challenge is how to mock Workflow.getInfo().isContinueAsNewSuggested() for testing purposes
                Workflow.continueAsNew(null);
            }
            return "done";
        }
    }


}
