package io.temporal.poc.sample

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.serviceclient.WorkflowServiceStubs


object SampleStarter {
    @JvmStatic
    fun main(args: Array<String>) {
        execute()
    }

    fun execute(): String {
        val service = WorkflowServiceStubs.newLocalServiceStubs()
        val client = WorkflowClient.newInstance(service)


        val workflow = client.newWorkflowStub(
            GreetingWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(SampleWorker.TASK_QUEUE)
                .setWorkflowId(WorkflowId.value)
                .build()
        )

        val result = workflow.greet("world")
        println("Result: $result")

        val historyAsJson = client.fetchHistory(WorkflowId.value).toJson(true)

        return historyAsJson
    }
}
