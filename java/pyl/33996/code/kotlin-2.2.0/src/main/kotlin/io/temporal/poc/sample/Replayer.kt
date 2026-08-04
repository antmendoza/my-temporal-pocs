package io.temporal.poc.sample

import io.temporal.testing.WorkflowReplayer
import java.io.File


object Replayer {
    @JvmStatic
    fun main(args: Array<String>) {
        val history = File(if (args.isNotEmpty()) args[0] else "history-kotlin-2.4.0.json")
        WorkflowReplayer.replayWorkflowExecution(history, GreetingWorkflowImpl::class.java)
        println("Replay OK: no non-determinism for ${history}")
    }
}