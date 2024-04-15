package io.antmendoza.samples.ui_wizard

import Workflow_5611
import Workflow_5611Impl
import io.temporal.api.common.v1.WorkflowExecution
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.testing.TestWorkflowRule
import io.temporal.worker.WorkerFactoryOptions
import io.temporal.workflow.Functions
import org.junit.Assert
import java.time.Duration
import java.time.Instant

class Workflow_5611_test_skiptime {
    @JvmField
    @org.junit.Rule
    var testWorkflowRule = TestWorkflowRule.newBuilder()
        .setWorkerFactoryOptions(
            WorkerFactoryOptions.newBuilder()
                .build()
        )
        .setWorkflowTypes(
            Workflow_5611Impl::class.java
        )
        .setUseTimeskipping(true) // default true
        .build()


    @org.junit.After
    fun after() {
        testWorkflowRule.testEnvironment.shutdown()
    }

    @org.junit.Test
    fun testHappyPath() {
        val workflowId = "my-workflow-" + Math.random()

        val workflowClient: WorkflowClient = testWorkflowRule.workflowClient
        val workflowExecution: Workflow_5611 = workflowClient.newWorkflowStub(
            Workflow_5611::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(testWorkflowRule.taskQueue)
                .setWorkflowId(workflowId)
                .build()
        )


        //start async
        WorkflowClient.start(Functions.Proc { workflowExecution.run() })

        val testWorkflowEnvironment = testWorkflowRule.testEnvironment
        println("[BEFORE SLEEP] Workflow time: ${Instant.ofEpochMilli(testWorkflowEnvironment.currentTimeMillis())}, Clock: ${Instant.now()}")

        //testWorkflowEnvironment.sleep(Duration.ofSeconds(2))
        println("[AFTER SLEEP ] Workflow time: ${Instant.ofEpochMilli(testWorkflowEnvironment.currentTimeMillis())}, Clock: ${Instant.now()}")

        val result = workflowClient.newUntypedWorkflowStub(workflowId).getResult(String::class.java)


        println("[Result ]  ${result}")

        Assert.assertEquals(result, "done")
    }

}