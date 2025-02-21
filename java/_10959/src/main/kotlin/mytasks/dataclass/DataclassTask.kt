package mytasks.dataclass

import simpletask.SimpleTask
import simpletask.SimpleTaskPayload

data class DataClassTaskInput(
  val input: String,
)

data class DataClassTaskOutput(
  val output: String,
)

class DataClassTask: SimpleTask<DataClassTaskInput, DataClassTaskOutput>() {
  override fun taskExecutionLogic(taskPayload: SimpleTaskPayload<DataClassTaskInput>): DataClassTaskOutput {

    return DataClassTaskOutput("Hello ${taskPayload.getPayload().input}")
  }
}