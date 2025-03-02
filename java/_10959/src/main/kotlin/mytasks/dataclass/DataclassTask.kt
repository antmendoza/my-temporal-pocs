package mytasks.dataclass

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import io.temporal.common.converter.GsonJsonPayloadConverter
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

    return DataClassTaskOutput("Hello ${taskPayload.getPayload().input}")
  }
}

fun main() {
  val converter = GsonJsonPayloadConverter()
  val payload = SimpleTaskPayload.Builder<DataClassTaskInput>()
    .withTaskPayload(DataClassTaskInput("test"))
    .build()
  val serialized = converter.toData(payload)
  val type = object : TypeToken<SimpleTaskPayload<DataClassTaskInput>>() {}.type
  val res = converter.fromData(serialized.get(), SimpleTaskPayload::class.java, type)
  println(res)
}