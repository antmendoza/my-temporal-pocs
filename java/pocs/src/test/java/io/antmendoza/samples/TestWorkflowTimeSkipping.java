package io.antmendoza.samples._5611;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;

class TestWorkflowTimeSkipping {


    @RegisterExtension
    TestWorkflowExtension testWorkflowExtension =
            TestWorkflowExtension.newBuilder()
                    .setWorkflowTypes(
                            Workflow_5611Impl.class
                    )
                    .setUseTimeskipping(true)
                    .setActivityImplementations(new Activity_5611Impl())
                    .build();


    @AfterEach
    public void after(TestWorkflowEnvironment testWorkflowEnvironment) {
        //System.out.println(testWorkflowEnvironment.getDiagnostics());
    }

    @org.junit.jupiter.api.Test
    public void testHappyPath(
            Workflow_5611 workflow,
            TestWorkflowEnvironment testWorkflowEnvironment,
            WorkflowClient workflowClient
    ) {

        WorkflowExecution execution = WorkflowClient.start(workflow::run);

        //repeat(3) {
        System.out.println("[BEFORE SLEEP] Workflow time: ${Instant.ofEpochMilli(testWorkflowEnvironment.currentTimeMillis())}, Clock: ${Instant.now()}");
        testWorkflowEnvironment.sleep(Duration.ofSeconds(5));
        System.out.println("[AFTER SLEEP ] Workflow time: ${Instant.ofEpochMilli(testWorkflowEnvironment.currentTimeMillis())}, Clock: ${Instant.now()}");

        String workflowId = execution.getWorkflowId();
        String result = workflowClient.newUntypedWorkflowStub(workflowId).getResult(String.class);
        System.out.println("[Result ]  ${result}");

        Assertions.assertEquals("done", result);
        //}

    }


}