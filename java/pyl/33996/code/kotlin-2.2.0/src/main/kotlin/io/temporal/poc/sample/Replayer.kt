package io.temporal.poc.sample

import io.temporal.testing.WorkflowReplayer
import java.io.File

/**
 * Replays a workflow history JSON (exported with `temporal workflow show --output json`, or via the
 * UI) against the GreetingWorkflowImpl compiled by THIS project's Kotlin version.
 *
 * Record a history with the 2.2.0 project, then replay it here and in the 2.4.0 project: the 2.4.0
 * build is expected to throw a non-determinism error where the START_CHILD / ACTIVITY command order
 * diverges from the recorded history.
 *
 * Usage: Replayer <history.json>
 */
object Replayer {
    @JvmStatic
    fun main(args: Array<String>) {
        require(args.isNotEmpty()) { "Usage: Replayer <history.json>" }
        val history = File(args[0])
        WorkflowReplayer.replayWorkflowExecution(history, GreetingWorkflowImpl::class.java)
        println("Replay OK: no non-determinism for ${args[0]}")
    }
}