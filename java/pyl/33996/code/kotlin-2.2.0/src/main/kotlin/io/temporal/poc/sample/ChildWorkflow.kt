package io.temporal.poc.sample

import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod



class GreetingChildWorkflowImpl : GreetingChildWorkflow {
    override fun emphasize(greeting: String): String {
        // Signal the parent that this sub-task was created (mirrors the child -> parent
        // subTaskCreated signal), so signals interleave with the parent's child-creation loop.
        val parentId = Workflow.getInfo().parentWorkflowId.orElseThrow()
        Workflow.newExternalWorkflowStub(GreetingWorkflow::class.java, parentId)
            .subTaskCreated(greeting)
        return "$greeting!"
    }
}

@WorkflowInterface
interface GreetingChildWorkflow {
    @WorkflowMethod
    fun emphasize(greeting: String): String
}
