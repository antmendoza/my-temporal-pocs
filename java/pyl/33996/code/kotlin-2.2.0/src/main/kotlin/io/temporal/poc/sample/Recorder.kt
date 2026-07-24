package io.temporal.poc.sample

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.client.WorkflowStub
import io.temporal.testing.TestWorkflowEnvironment
import java.io.File

/**
 * Records a workflow history JSON using an in-memory, time-skipping [TestWorkflowEnvironment] - no
 * external `temporal server` is required.
 *
 * The workflow self-drives: [GreetingWorkflowImpl.greet] creates children in a loop and each child
 * signals the parent back (`subTaskCreated`). Those signals interleave with the create loop, so a
 * single workflow task ends up containing BOTH a child-start (main thread) and a notification
 * activity (signal-handler callback thread) - the point where the command order can differ between
 * Kotlin compiler versions.
 *
 * Record with this (Kotlin 2.2.0) build, then replay the JSON with the Kotlin 2.4.0 build's
 * [Replayer] to surface the non-determinism.
 *
 * Usage: Recorder [outputHistory.json]
 */
object Recorder {
    @JvmStatic
    fun main(args: Array<String>) {
        val out = File(if (args.isNotEmpty()) args[0] else "history-kotlin-2.2.0.json")

        val env = TestWorkflowEnvironment.newInstance()
        val worker = env.newWorker(SampleWorker.TASK_QUEUE)
        worker.registerWorkflowImplementationTypes(
            GreetingWorkflowImpl::class.java,
            GreetingChildWorkflowImpl::class.java
        )
        worker.registerActivitiesImplementations(GreetingActivitiesImpl())
        env.start()

        val client = env.workflowClient
        val stub = client.newWorkflowStub(
            GreetingWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(SampleWorker.TASK_QUEUE)
                .setWorkflowId("sample-record")
                .build()
        )

        // The workflow self-drives: it creates children in a loop and each child signals the parent
        // back, so no external signal is needed here.
        val exec = WorkflowClient.start(stub::greet, "world")

        val result = WorkflowStub.fromTyped(stub).getResult(String::class.java)
        println("Workflow completed with result: $result")

        val historyJson = env.getWorkflowExecutionHistory(exec).toJson(true)
        out.writeText(historyJson)
        println("Wrote history to ${out.absolutePath}")

        env.close()
    }
}
