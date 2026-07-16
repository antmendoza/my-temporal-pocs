package io.temporal.poc.sample

import io.temporal.client.WorkflowClient
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.WorkerFactory

object SampleWorker {
    const val TASK_QUEUE = "sample-queue"

    @JvmStatic
    fun main(args: Array<String>) {
        val service = WorkflowServiceStubs.newLocalServiceStubs()
        val client = WorkflowClient.newInstance(service)

        val factory = WorkerFactory.newInstance(client)
        val worker = factory.newWorker(TASK_QUEUE)

        worker.registerWorkflowImplementationTypes(
            GreetingWorkflowImpl::class.java,
            GreetingChildWorkflowImpl::class.java
        )
        worker.registerActivitiesImplementations(GreetingActivitiesImpl())

        factory.start()
        println("Worker started, polling '$TASK_QUEUE'")
    }
}