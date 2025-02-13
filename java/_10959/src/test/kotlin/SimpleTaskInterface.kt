package kotlin

import io.temporal.activity.ActivityInterface
import io.temporal.activity.DynamicActivity
import io.temporal.common.converter.EncodedValues
import io.temporal.testing.TestActivityEnvironment

@ActivityInterface
interface SimpleTaskInterface<T, K : Any> {
  fun taskExecutionLogic(taskPayload: SimpleTaskPayload<T>): K
}

class SimpleTaskTestWrapper<T, K : Any>(private val task: SimpleTask<T, K>) : DynamicActivity {
  override fun execute(args: EncodedValues): Any {
    @Suppress("UNCHECKED_CAST")
    val simpleTaskPayload = args.get(SimpleTaskPayload::class.java) as SimpleTaskPayload<T>
    return task.taskExecutionLogic(simpleTaskPayload)
  }
}

class SimpleTaskTestEnvironment<T, K : Any>(private val task: SimpleTask<T, K>) {
  private val testActivityEnvironment = TestActivityEnvironment.newInstance()

  init {
    testActivityEnvironment.registerActivitiesImplementations(SimpleTaskTestWrapper(task))
  }
  fun taskStub(): SimpleTaskInterface<T, Any> {
    val newActivityStub = testActivityEnvironment.newActivityStub(SimpleTaskInterface::class.java)
    return newActivityStub as SimpleTaskInterface<T, Any>
  }
}