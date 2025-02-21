package mytasks.simplecase

import mytasks.jackson.JacksonTask
import mytasks.jackson.JacksonTaskInput
import mytasks.jackson.JacksonTaskOutput
import simpletask.SimpleTaskPayload
import simpletask.SimpleTaskTemporalConfig
import simpletask.StartSimpleTaskParams

fun main() {
  val task = PrimitiveTask()
  task.buildSimpleTask(
    SimpleTaskTemporalConfig(
      temporalTargetEndpoint = "localhost:7233",
      temporalNamespace = "default",
      taskQueue = "primitive-task-queue",
    )
  )

  task.spinUpSimpleTaskWorker()

  try {
    val payload = SimpleTaskPayload.Builder<String>()
      .withTaskPayload("test")
      .build()

    val params = StartSimpleTaskParams(
      payload = payload,
      workflowId = "primitive-task-test",
      resultClass = String::class
    )

    val result = task.executeTaskAndWaitForResult(params)
    println("Task returned: $result")
  } finally {
    task.shutdown()
  }
}