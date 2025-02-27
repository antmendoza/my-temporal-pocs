package mytasks.dataclass

import com.google.gson.annotations.SerializedName
import simpletask.SimpleTask
import simpletask.SimpleTaskPayload

data class DataClassTaskInput(
  val input: String,
)

data class DataClassTaskOutput(
  @SerializedName("output")  val output: String,
)

class DataClassTask: SimpleTask<DataClassTaskInput, DataClassTaskOutput>() {
  override fun taskExecutionLogic(taskPayload: SimpleTaskPayload<DataClassTaskInput>): DataClassTaskOutput {


    System.out.println("taskPayload " + taskPayload)
    System.out.println("taskPayload.getPayload() " + taskPayload.getPayload())
    System.out.println("taskPayload.getPayload() " + taskPayload.getPayload().input)
    System.out.println("result " + DataClassTaskOutput("Hello ${taskPayload.getPayload().input}"))



    return DataClassTaskOutput("Hello ${taskPayload.getPayload().input}")
  }
}