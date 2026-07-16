package io.temporal.poc.sample

import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Async
import io.temporal.workflow.QueryMethod
import io.temporal.workflow.SignalMethod
import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import java.time.Duration

@WorkflowInterface
interface GreetingWorkflow {
    @WorkflowMethod
    fun greet(name: String): String

    @SignalMethod
    fun signal(name: String)

    @QueryMethod
    fun query(): String
}

class GreetingWorkflowImpl : GreetingWorkflow {

    private val activities = Workflow.newActivityStub(
        GreetingActivities::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )

    private var signaled = false

    override fun greet(name: String): String {
        Workflow.sleep(Duration.ofSeconds(1))
        Workflow.await { signaled }

        val child = Workflow.newChildWorkflowStub(GreetingChildWorkflow::class.java)
        val childResult = Async.function(child::emphasize, name)

        return childResult.get()
    }

    override fun signal(name: String) {


        val notify = Async.function(activities::composeGreeting, name)

        signaled = true
    }

    override fun query(): String {

        return "Hello"
    }
}