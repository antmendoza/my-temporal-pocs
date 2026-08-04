package io.temporal.poc.sample

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import java.io.File


object Query {
    @JvmStatic
    fun main(args: Array<String>) {
        val workflowId = if (args.isNotEmpty()) args[0] else "sample-kotlin-2.2.0"

        execute(workflowId)
    }

    fun execute(workflowId: String): String {

        println("\n \n \n >>>>>>>> Querying workflow $workflowId")

        val service = WorkflowServiceStubs.newLocalServiceStubs()
        val client = WorkflowClient.newInstance(service)

        val workflow = client.newUntypedWorkflowStub(workflowId)
        val query = workflow.query("query", String::class.java)
        println("\n \n \n >>>>>>> result $query")
        return query;


    }
}
