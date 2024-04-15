package io.antmendoza.samples.ui_wizard

import Workflow_5611
import Workflow_5611Impl
import io.mockk.junit5.MockKExtension
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.common.converter.DefaultDataConverter
import io.temporal.testing.TestWorkflowEnvironment
import io.temporal.testing.TestWorkflowExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Duration
import java.time.Instant

@ExtendWith(MockKExtension::class)
class Workflow_5611_test_skiptime_2 {


    @JvmField
    @RegisterExtension
    val testWorkflowExtension: TestWorkflowExtension =
        TestWorkflowExtension.newBuilder()
            .setWorkflowTypes(
                Workflow_5611Impl::class.java,
            )
            .setWorkflowClientOptions(workflowClientOptions())
            .setUseTimeskipping(true)
            .useInternalService()
            .build()


    @org.junit.jupiter.api.Test
    fun testHappyPath(
        workflow: Workflow_5611,
        testWorkflowEnvironment: TestWorkflowEnvironment,
        workflowClient: WorkflowClient
    ) {

        val execution = WorkflowClient.start(workflow::run)


        //repeat(3) {
        println("[BEFORE SLEEP] Workflow time: ${Instant.ofEpochMilli(testWorkflowEnvironment.currentTimeMillis())}, Clock: ${Instant.now()}")
        testWorkflowEnvironment.sleep(Duration.ofSeconds(5))
        println("[AFTER SLEEP ] Workflow time: ${Instant.ofEpochMilli(testWorkflowEnvironment.currentTimeMillis())}, Clock: ${Instant.now()}")

        val result = workflowClient.newUntypedWorkflowStub(execution.workflowId).getResult(String::class.java)
        println("[Result ]  ${result}")

        Assertions.assertEquals("done", result)
        //}



    }


    fun workflowClientOptions(): WorkflowClientOptions =
        WorkflowClientOptions.newBuilder()
            .setDataConverter(
                DefaultDataConverter
                    .newDefaultInstance()
                    // register all the standard converters from the default instance, but customize Jackson for kotlin
                    //.withPayloadConverterOverrides(CustomJacksonJsonPayloadConverter)
            )
            .build()

}