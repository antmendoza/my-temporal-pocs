package mytasks.delayed

import io.temporal.client.WorkflowClientOptions
import io.temporal.common.converter.DefaultDataConverter
import io.temporal.testing.TestEnvironmentOptions
import io.temporal.testing.TestWorkflowEnvironment
import io.temporal.workflow.Workflow
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
import kotlin.test.assertEquals


class DelayedTaskTest {
    private lateinit var testWorkflowEnvironment: TestWorkflowEnvironment
    private lateinit var task: DelayedTask
    private var now: Long = 0L

    @Before
    fun setUp() {
        testWorkflowEnvironment = providesTestWorkflowEnvironment()
        task = providesTask(
          providesSimpleTaskClientManager(testWorkflowEnvironment),
          providesSimpleTaskWorkerManager(testWorkflowEnvironment),
        )
        val worker = testWorkflowEnvironment.newWorker("delayed-task-queue")

        worker.registerWorkflowImplementationTypes(task.getWorkflowClass())
        worker.registerActivitiesImplementations(task.CashActivity())
        now = testWorkflowEnvironment.currentTimeMillis()
        testWorkflowEnvironment.start()
        task.spinUpSimpleTaskWorker()
    }

    fun providesTestWorkflowEnvironment(): TestWorkflowEnvironment {
        val workflowClientOptions = WorkflowClientOptions.newBuilder().build()!!

        return TestWorkflowEnvironment.newInstance(
          TestEnvironmentOptions.newBuilder().setWorkflowClientOptions(workflowClientOptions).build()
        )
    }

    fun providesSimpleTaskClientManager(testWorkflowEnvironment: TestWorkflowEnvironment): SimpleTaskClientManager {
        return SimpleTaskClientManager(
          workflowClient = testWorkflowEnvironment.workflowClient,
          taskQueue = "delayed-task-queue",
        )
    }

    fun providesSimpleTaskWorkerManager(testWorkflowEnvironment: TestWorkflowEnvironment): SimpleTaskWorkerManager {
        return SimpleTaskWorkerManager(testWorkflowEnvironment.workerFactory)
    }

    fun providesTask(
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
        task.shutdown()
    }

    @Test
    fun `with timer in workflow`() {
        val payload = SimpleTaskPayload.Builder<String>()
          .withTaskPayload("Test")
          .withDelayMilliseconds(5000)
          .withVersion("1")
          .build()

        val params = StartSimpleTaskParams(
          payload = payload,
          workflowId = "delayed-task-test",
          resultClass = String::class
        )

        task.start(params)
        testWorkflowEnvironment.sleep(Duration.ofMillis(3000))
        println("Done first sleeping")
        testWorkflowEnvironment.sleep(Duration.ofMillis(3000))
    }

    @Test
    fun `with delayed start`() {
        val payload = SimpleTaskPayload.Builder<String>()
          .withTaskPayload("Test")
          .withDelayMilliseconds(5000)
          .withVersion("2")
          .build()

        val params = StartSimpleTaskParams(
          payload = payload,
          workflowId = "delayed-task-test",
          resultClass = String::class
        )

        task.startDelayed(params)
        testWorkflowEnvironment.sleep(Duration.ofMillis(3000))
        println("Done first sleeping")
        testWorkflowEnvironment.sleep(Duration.ofMillis(3000))
    }
}
