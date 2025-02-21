package mytasks.dataclass

import mytasks.jackson.JacksonTask
import mytasks.jackson.JacksonTaskInput
import mytasks.jackson.JacksonTaskOutput
import simpletask.SimpleTaskPayload
import simpletask.SimpleTaskTemporalConfig
import simpletask.StartSimpleTaskParams
import simpletask.createWireGsonJsonDataConverter

fun main() {
  val task = DataClassTask()
  task.buildSimpleTask(
    SimpleTaskTemporalConfig(
      temporalTargetEndpoint = "localhost:7233",
      temporalNamespace = "default",
      taskQueue = "dataclass-task-queue",
      dataConverter = createWireGsonJsonDataConverter(),
    )
  )

  task.spinUpSimpleTaskWorker()

  try {
    val payload = SimpleTaskPayload.Builder<DataClassTaskInput>()
      .withTaskPayload(DataClassTaskInput("test"))
      .build()

    val params = StartSimpleTaskParams(
      payload = payload,
      workflowId = "dataclass-task-test",
      resultClass = DataClassTaskOutput::class
    )

    val result = task.executeTaskAndWaitForResult(params)
    println("Task returned: ${result.output}")
  } finally {
    task.shutdown()
  }
}