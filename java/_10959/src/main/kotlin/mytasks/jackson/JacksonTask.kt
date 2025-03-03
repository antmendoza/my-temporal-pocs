package mytasks.jackson

import com.fasterxml.jackson.annotation.JsonProperty
import simpletask.SimpleTask
import simpletask.SimpleTaskPayload

class JacksonTaskInput(
  @JsonProperty("input") val input: String,
)

class JacksonTaskOutput(
  @JsonProperty("output") val output: String,
)

class JacksonTask: SimpleTask<JacksonTaskInput, JacksonTaskOutput>() {
  override fun taskExecutionLogic(taskPayload: SimpleTaskPayload<JacksonTaskInput>): JacksonTaskOutput {

    return JacksonTaskOutput("Hello ${taskPayload.getPayload().input}")
  }
}