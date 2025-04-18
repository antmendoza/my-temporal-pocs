package mytasks.delayed

import io.nexusrpc.handler.OperationStartResult.async
import io.temporal.client.WorkflowClientOptions
import io.temporal.common.converter.DefaultDataConverter
import io.temporal.testing.TestEnvironmentOptions
import io.temporal.testing.TestWorkflowEnvironment
import io.temporal.workflow.Workflow
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mytasks.jackson.JacksonTaskInput
import mytasks.jackson.JacksonTaskOutput
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import simpletask.SimpleTaskClientManager
import simpletask.SimpleTaskPayload
import simpletask.SimpleTaskWorkerManager
import simpletask.StartSimpleTaskParams
import testing.SimpleTaskTestEnvironment
import java.time.Duration
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class DelayedTaskTest {
    private lateinit var testWorkflowEnvironment: TestWorkflowEnvironment
    private lateinit var task1: DelayedTask
    private lateinit var task2: DelayedTask
    private var now: Long = 0L

    @Before
    fun setUp() {
        testWorkflowEnvironment = providesTestWorkflowEnvironment()
        task1 = createTaskAndWorker("task-queue-1")
        task2 = createTaskAndWorker("task-queue-2")
        now = testWorkflowEnvironment.currentTimeMillis()
        testWorkflowEnvironment.start()
    }

    private fun createTaskAndWorker(taskQueue: String): DelayedTask {
        val task = providesTask(
          providesSimpleTaskClientManager(testWorkflowEnvironment, taskQueue),
          providesSimpleTaskWorkerManager(testWorkflowEnvironment),
        )
        val worker = testWorkflowEnvironment.newWorker(taskQueue)

        worker.registerWorkflowImplementationTypes(task.getWorkflowClass())
        worker.registerActivitiesImplementations(task.CashActivity())

        return task
    }

    private fun providesTestWorkflowEnvironment(): TestWorkflowEnvironment {
        val workflowClientOptions = WorkflowClientOptions.newBuilder().build()!!

        return TestWorkflowEnvironment.newInstance(
          TestEnvironmentOptions.newBuilder().setWorkflowClientOptions(workflowClientOptions).build()
        )
    }

    private fun providesSimpleTaskClientManager(
        testWorkflowEnvironment: TestWorkflowEnvironment,
        taskQueue: String,
    ): SimpleTaskClientManager {
        return SimpleTaskClientManager(
          workflowClient = testWorkflowEnvironment.workflowClient,
          taskQueue = taskQueue,
        )
    }

    private fun providesSimpleTaskWorkerManager(testWorkflowEnvironment: TestWorkflowEnvironment): SimpleTaskWorkerManager {
        return SimpleTaskWorkerManager(testWorkflowEnvironment.workerFactory)
    }

    private fun providesTask(
        simpleTaskClientManager: SimpleTaskClientManager,
        simpleTaskWorkerManager: SimpleTaskWorkerManager,
    ): DelayedTask {
        val task = DelayedTask()
        task.buildSimpleTask(simpleTaskClientManager, simpleTaskWorkerManager)
        return task
    }

    @After
    fun tearDown() {
        testWorkflowEnvironment.shutdown()
    }

    @Test
    fun `with timer in workflow`() {
        runTest("1")
    }

    @Test
    fun `with delayed start`() {
        runTest("2")
    }

    fun runTest(version: String) {
        val anotherTask = providesTask(
          providesSimpleTaskClientManager(testWorkflowEnvironment, "task-queue-1"),
          providesSimpleTaskWorkerManager(testWorkflowEnvironment),
        )
        listOf(task1, anotherTask).parallelMapIndexed { index, task ->
            task.start(StartSimpleTaskParams(
              payload = SimpleTaskPayload.Builder<String>()
                .withTaskPayload("Task$index")
                .withDelayMilliseconds(10L.minutes.inWholeMilliseconds)
                .withVersion(version)
                .build(),
              workflowId = "task$index",
              resultClass = String::class
            ))
        }
        task2.start(StartSimpleTaskParams(
          payload = SimpleTaskPayload.Builder<String>()
            .withTaskPayload("Task2")
            .withDelayMilliseconds(20L.minutes.inWholeMilliseconds)
            .withVersion(version)
            .build(),
          workflowId = "task2",
          resultClass = String::class
        ))
        println("Sleeping 5 minutes")
        testWorkflowEnvironment.sleep(5.minutes.toJavaDuration())
        println("Done first sleeping, sleeping another 10 minutes")
        testWorkflowEnvironment.sleep(10.minutes.toJavaDuration())
        println("Done second sleeping, sleeping another 10 minutes")
        testWorkflowEnvironment.sleep(10.minutes.toJavaDuration())
    }

    fun <T1, T2> Iterable<T1>.parallelMapIndexed(
        mapper: suspend (Int, T1) -> T2,
    ): List<T2> = runBlocking {
        // Do the mapping operation in parallel.
        mapIndexed { index, value ->
            async { mapper(index, value) }
        }
          // Await all the coroutines for completion, which returns the mapped value.
          .map { it.await() }
    }
}
