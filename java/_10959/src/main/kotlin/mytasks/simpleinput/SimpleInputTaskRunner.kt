package mytasks.simpleinput

import mytasks.dataclass.DataClassTask
import mytasks.dataclass.DataClassTaskInput
import mytasks.dataclass.DataClassTaskOutput
import simpletask.SimpleTaskPayload
import simpletask.SimpleTaskTemporalConfig
import simpletask.StartSimpleTaskParams
import simpletask.createWireGsonJsonDataConverter

fun main() {
  val task = SimpleInputTask()
  task.buildSimpleTask(
    SimpleTaskTemporalConfig(
      temporalTargetEndpoint = "localhost:7233",
      temporalNamespace = "default",
      taskQueue = "simpleinput-task-queue",
      dataConverter = createWireGsonJsonDataConverter(),
    )
  )

  task.spinUpSimpleTaskWorker()

  try {
    val payload = SimpleTaskPayload.Builder<String>()
      .withTaskPayload("test")
      .build()

    val params = StartSimpleTaskParams(
      payload = payload,
      workflowId = "simpleinput-task-test",
      resultClass = SimpleInputTaskOutput::class
    )

    val result = task.executeTaskAndWaitForResult(params)
    println("Task returned: ${result.output}")
  } finally {
    task.shutdown()
  }
}