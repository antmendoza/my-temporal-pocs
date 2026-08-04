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

        val useLambda = System.getenv().getOrDefault("USE_LAMBDA", "true")
        val promise:Promise<String>;
        if(useLambda.toBoolean()){
            promise = Async.function{child.emphasize(assignee)}
        }
        else{

            // START_CHILD via a method reference to the child stub interface method
            promise = Async.function(child::emphasize, assignee)
        }

        Workflow.getWorkflowExecution(child).get()
        childPromises.add(promise)

    }

    override fun subTaskCreated(assignee: String) {
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