package mytasks.simpleinput

import simpletask.SimpleTask
import simpletask.SimpleTaskPayload

data class SimpleInputTaskOutput(
  val output: String,
)

class SimpleInputTask: SimpleTask<String, SimpleInputTaskOutput>() {
  override fun taskExecutionLogic(taskPayload: SimpleTaskPayload<String>): SimpleInputTaskOutput {

    return SimpleInputTaskOutput("Hello ${taskPayload.getPayload()}")
  }
}