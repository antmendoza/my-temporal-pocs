package io.temporal.poc.sample

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import java.util.UUID

object SampleStarter {
    @JvmStatic
    fun main(args: Array<String>) {
        val service = WorkflowServiceStubs.newLocalServiceStubs()
        val client = WorkflowClient.newInstance(service)

        val workflow = client.newWorkflowStub(
            GreetingWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(SampleWorker.TASK_QUEUE)
                .setWorkflowId("sample-kotlin-2.2.0")
                .build()
        )

        WorkflowClient.start(workflow::greet, "")

        Thread.sleep(2_000)
        workflow.signal("test")

        Thread.sleep(40_000)
        println("Result: ")
    }
}