package io.temporal.poc.sample

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.serviceclient.WorkflowServiceStubs

/**
 * Starts the workflow against a real (local) `temporal server`. The workflow self-drives: it creates
 * children in a loop and each child signals the parent back, so no external signal is needed.
 *
 * For a self-contained reproduction that needs no server, use [Recorder] (records a history via an
 * in-memory test environment) and [Replayer] (replays it).
 */
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
