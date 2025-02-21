package testing

import io.temporal.activity.ActivityInterface
import simpletask.SimpleTaskPayload

@ActivityInterface
interface SimpleTaskInterface<T, K : Any> {
  fun taskExecutionLogic(taskPayload: SimpleTaskPayload<T>): K
}
