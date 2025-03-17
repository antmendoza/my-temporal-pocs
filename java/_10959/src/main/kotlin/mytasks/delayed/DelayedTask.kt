package mytasks.delayed

import simpletask.SimpleTask
import simpletask.SimpleTaskPayload

class DelayedTask : SimpleTask<String, String>() {
  override fun taskExecutionLogic(taskPayload: SimpleTaskPayload<String>): String {
    println("Hello ${taskPayload.getPayload()}")
    return "Hello ${taskPayload.getPayload()}"
  }
}