package com.testimport

import com.test.GreetingActivities
import com.test.GreetingActivitiesImpl
import com.test.MyChildWorkflow
import com.test.MyChildWorkflowImpl
import io.temporal.activity.ActivityOptions

import io.temporal.api.common.v1.WorkflowExecution
import io.temporal.api.enums.v1.ParentClosePolicy
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.internal.sync.WorkflowInternal
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.WorkerFactory
import io.temporal.worker.WorkerFactoryOptions
import io.temporal.workflow.*
import java.time.Duration

/** Sample Temporal Workflow Definition that executes a single Activity.  */
object WorkflowRunner {
    // Define the task queue name
    const val TASK_QUEUE: String = "HelloActivityTaskQueue"

    // Define our workflow unique id
    const val WORKFLOW_ID: String = "Kotlin-Workflow"

    /**
     * With our Workflow and Activities defined, we can now start execution. The main method starts
     * the worker and then the workflow.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        // Get a Workflow service stub.

        val service = WorkflowServiceStubs.newLocalServiceStubs()

        val client = WorkflowClient.newInstance(service)

        val factory = WorkerFactory.newInstance(
            client,
            WorkerFactoryOptions.newBuilder().setWorkflowCacheSize(0).build()
        )

        val worker = factory.newWorker(TASK_QUEUE)

        worker.registerWorkflowImplementationTypes(
            MyWorkflowImpl::class.java,
            MyChildWorkflowImpl::class.java
        )

        worker.registerActivitiesImplementations(GreetingActivitiesImpl())

        factory.start()

        // Create the workflow client stub. It is used to start our workflow execution.
        val workflow =
            client.newWorkflowStub(
                MyWorkflow::class.java,
                WorkflowOptions.newBuilder()
                    .setWorkflowId(WORKFLOW_ID)
                    .setTaskQueue(TASK_QUEUE)
                    .build()
            )

        val greeting = workflow.getGreeting("World")

        // Display workflow execution results
        println(greeting)


        try {
            workflow.query("")
        } catch (e: Exception) {
            e.printStackTrace()

        }

        System.exit(0);
    }

    fun createPromiseChildWorkflow(
        name: String?, childWorkflowId: String?
    ): Promise<WorkflowExecution> {
        val child = createAsyncChildWorkflow(name, childWorkflowId)
        val childExecution = Workflow.getWorkflowExecution(child)
        return childExecution
    }

    fun createAsyncChildWorkflow(name: String?, childWorkflowId: String?): MyChildWorkflow {
        val child =
            Workflow.newChildWorkflowStub(
                MyChildWorkflow::class.java,
                ChildWorkflowOptions.newBuilder()
                    .setWorkflowId(childWorkflowId)
                    .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_ABANDON)
                    .build()
            )

        Async.function({ name: String? -> child.getGreeting(name) }, name)
        return child
    }

    @WorkflowInterface
    interface MyWorkflow {
        @WorkflowMethod
        fun getGreeting(name: String?): String

        @QueryMethod
        fun query(s: String): String
    }

    class MyWorkflowImpl : MyWorkflow {
        private val activities: GreetingActivities = Workflow.newActivityStub(
            GreetingActivities::class.java,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build()
        )

        override fun getGreeting(name: String?): String {
            val childWorkflow1 = "child_workflow_1_" + Workflow.currentTimeMillis()

            val child_1 = createAsyncChildWorkflow(name, childWorkflow1)
            // Wait for child to start
            Workflow.getWorkflowExecution(child_1).get()

            //      1	call an activity
            // This is a blocking call that returns only after the activity has completed.
            val hello = activities.composeGreeting("Hello", name)

            //      2	signal external workflow
            child_1.signalHandler(Math.random().toString() + "")

            //      Workflow.newUntypedExternalWorkflowStub(childWorkflow1).signal("signal_1", "value_1");

            //      3	start child workflow using Async.function
            val childWorkflow2 = "child_workflow_2_" + Workflow.currentTimeMillis()
            val child_2 = createAsyncChildWorkflow(name, childWorkflow2)


            if(WorkflowInternal.isReplaying()){


                //      4	use getVersion
                Workflow.getVersion("get-child-workflow", Workflow.DEFAULT_VERSION, 1);

            }
            //      5	query execution details of the started child workflow in step 3 if version == 1
            // (i.e. not default version)

            //      6 signal external workflow W1 again
            child_1.signalHandler(Math.random().toString() + "")

            return hello
        }

        override fun query(s: String): String {
            return "-"
        }
    }
}