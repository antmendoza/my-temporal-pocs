package testing

import io.temporal.client.WorkflowClientOptions
import io.temporal.common.converter.DataConverter
import io.temporal.testing.TestActivityEnvironment
import io.temporal.testing.TestEnvironmentOptions
import simpletask.SimpleTask
import simpletask.SimpleTaskPayload
import simpletask.createWireGsonJsonDataConverter
import java.lang.reflect.Type

/**
 * A test environment to simplify testing [SimpleTask]s
 *
 * Example usage:
 * val task = MySimpleTask()
 * val testEnv = SimpleTaskTestEnvironment(task)
 *
 * val payload = SimpleTaskPayload.Builder<T>()...build()
 * val result = testEnv.executeSimpleTask(payload, resultClass, resultType)
 * assert(result == expectedResult)
 *
 */
class SimpleTaskTestEnvironment<T, K : Any> @JvmOverloads constructor(
  val task: SimpleTask<T, K>,
  val dataConverter: DataConverter = createWireGsonJsonDataConverter(),
) {
  val testActivityEnvironment: TestActivityEnvironment

  init {
    val clientOptions = WorkflowClientOptions.newBuilder().setDataConverter(dataConverter).build()
    val options = TestEnvironmentOptions.newBuilder().setWorkflowClientOptions(clientOptions).build()
    testActivityEnvironment = TestActivityEnvironment.newInstance(options)
    testActivityEnvironment.registerActivitiesImplementations(task.CashActivity())
  }
  private fun taskStub(): SimpleTaskInterface<T, K> {
    @Suppress("UNCHECKED_CAST")
    return testActivityEnvironment.newActivityStub(SimpleTaskInterface::class.java) as SimpleTaskInterface<T, K>
  }

  fun executeSimpleTask(taskPayload: SimpleTaskPayload<T>, resultClass: Class<K>, resultType: Type): K {
    val result = taskStub().taskExecutionLogic(taskPayload)
    val payload = dataConverter.toPayload(result)
    return dataConverter.fromPayload(payload.get(), resultClass, resultType)
  }
}
