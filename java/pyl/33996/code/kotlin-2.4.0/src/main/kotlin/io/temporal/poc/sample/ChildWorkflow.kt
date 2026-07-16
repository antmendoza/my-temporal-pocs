package io.temporal.poc.sample

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface GreetingChildWorkflow {
    @WorkflowMethod
    fun emphasize(greeting: String): String
}

class GreetingChildWorkflowImpl : GreetingChildWorkflow {
    override fun emphasize(greeting: String): String = "$greeting!"
}