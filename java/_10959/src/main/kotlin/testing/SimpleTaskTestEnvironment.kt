package testing

import io.temporal.client.WorkflowClientOptions
import io.temporal.common.converter.DataConverter
import io.temporal.testing.TestActivityEnvironment
import io.temporal.testing.TestEnvironmentOptions
import simpletask.SimpleTask
import simpletask.createWireGsonJsonDataConverter

/**
 * A test environment to simplify testing [SimpleTask]s
 *
 * Example usage:
 * val task = MySimpleTask()
 * val testEnv = SimpleTaskTestEnvironment(task)
 *
 * val payload = SimpleTaskPayload.Builder<T>()...build()
 * val result = testEnv.taskStub().taskExecutionLogic(payload)
 * assert(result == expectedResult)
 *
 */
class SimpleTaskTestEnvironment<T, K : Any> @JvmOverloads constructor(
  task: SimpleTask<T, K>,
  dataConverter: DataConverter = createWireGsonJsonDataConverter(),
) {
  val testActivityEnvironment: TestActivityEnvironment

  init {
    val clientOptions = WorkflowClientOptions.newBuilder().setDataConverter(dataConverter).build()
    val options = TestEnvironmentOptions.newBuilder().setWorkflowClientOptions(clientOptions).build()
    testActivityEnvironment = TestActivityEnvironment.newInstance(options)
    testActivityEnvironment.registerActivitiesImplementations(task.CashActivity())
  }
  fun taskStub(): SimpleTaskInterface<T, K> {
    @Suppress("UNCHECKED_CAST")
    return testActivityEnvironment.newActivityStub(SimpleTaskInterface::class.java) as SimpleTaskInterface<T, K>
  }
}
