package mytasks.simplecase

import mytasks.dataclass.DataClassTaskInput
import mytasks.dataclass.DataClassTaskOutput
import simpletask.SimpleTask
import simpletask.SimpleTaskPayload

class PrimitiveTask: SimpleTask<String, String>() {
  override fun taskExecutionLogic(taskPayload: SimpleTaskPayload<String>): String {

    return "Hello ${taskPayload.getPayload()}"
  }
}