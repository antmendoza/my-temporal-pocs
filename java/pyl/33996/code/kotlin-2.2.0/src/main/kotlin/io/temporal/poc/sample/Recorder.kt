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



        val worker = SampleWorker;
        worker.main(arrayOf());


        val historyAsJson = SampleStarter.execute()

        out.writeText(historyAsJson)
        println("Wrote history to ${out.absolutePath}")


        worker.shutdown();


    }
}
