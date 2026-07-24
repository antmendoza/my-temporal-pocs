package io.temporal.poc.sample

import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Async
import io.temporal.workflow.Promise
import io.temporal.workflow.QueryMethod
import io.temporal.workflow.SignalMethod
import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import java.time.Duration


/**
 * Minimal reproduction of the GroupTaskWorkflow / GroupTaskHandler command-ordering non-determinism.
 *
 * The shape that matters:
 *   - The MAIN workflow thread creates children in a loop. Each child is started with
 *     `Async.function(child::emphasize, ...)` - a method reference to the child stub interface method -
 *     and the loop then blocks on `Workflow.getWorkflowExecution(child).get()`, so there is one child
 *     per workflow task (exactly like GroupTaskHandler.createChildTask using `child::startWorkflow`).
 *   - Each child signals the parent back (`subTaskCreated`) as it starts, so signals interleave with
 *     the create loop.
 *   - The signal handler runs on a CALLBACK WorkflowThread and, when an assignee's counter reaches
 *     zero, schedules a notification activity via `Async.function { ... }` (a lambda).
 *
 * When a single workflow task contains both a child-start (main thread) and a notification
 * (callback thread), the two commands are produced by different WorkflowThreads. Their relative order
 * is decided by which thread the Temporal DeterministicRunner advances first - and that turned out to
 * be sensitive to how the `child::emphasize` method reference is lowered by the Kotlin compiler:
 *   - Kotlin 2.2.0: [START_CHILD, ACTIVITY]
 *   - Kotlin 2.4.0: [ACTIVITY, START_CHILD]
 * so a history recorded on 2.2.0 fails to replay on 2.4.0 with a non-determinism error.
 */
class GreetingWorkflowImpl : GreetingWorkflow {

    private val activities = Workflow.newActivityStub(
        GreetingActivities::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )

    // assignee -> number of child sub-tasks still to be created before its batched notification fires.
    private val remainingByAssignee = mutableMapOf<String, Int>()
    private val childPromises = mutableListOf<Promise<String>>()

    override fun greet(name: String): String {
        val assignees = (1..CHILD_COUNT).map { "assignee-$it" }
        assignees.forEach { remainingByAssignee[it] = 1 }

        assignees.forEachIndexed { idx, assignee -> createChild(idx + 1, assignee) }

        Promise.allOf(childPromises).get()
        return "created ${childPromises.size} children"
    }

    /** Mirrors GroupTaskHandler.createChildTask: one child per workflow task. */
    private fun createChild(index: Int, assignee: String) {
        val child = Workflow.newChildWorkflowStub(GreetingChildWorkflow::class.java)
        // START_CHILD via a method reference to the child stub interface method - this is the
        // reference whose Kotlin lowering changed between 2.2.0 (REF_invokeInterface) and 2.4.0
        // (static bridge via REF_invokeStatic).
        val promise = Async.function{child.emphasize(assignee)}
        // Block until the child has started so there is one child per workflow task and the child's
        // signal-back can interleave with the next child start.
        Workflow.getWorkflowExecution(child).get()
        childPromises.add(promise)
    }

    override fun subTaskCreated(assignee: String) {
        // Runs on a callback WorkflowThread. When the last sub-task for an assignee is created, fire
        // the batched notification activity via a lambda - a DIFFERENT WorkflowThread from the loop.
        val remaining = remainingByAssignee.computeIfPresent(assignee) { _, v -> v - 1 }
        if (remaining == 0) {
            Async.function { activities.composeGreeting(assignee) }
        }
    }

    override fun query(): String {
        return "some query result"
    }

    companion object {
        private const val CHILD_COUNT = 20
    }
}



@WorkflowInterface
interface GreetingWorkflow {
    @WorkflowMethod
    fun greet(name: String): String

    /** Signalled by each child once it has started (mirrors GroupTaskWorkflow.subTaskCreated). */
    @SignalMethod
    fun subTaskCreated(assignee: String)

    @QueryMethod
    fun query(): String

}