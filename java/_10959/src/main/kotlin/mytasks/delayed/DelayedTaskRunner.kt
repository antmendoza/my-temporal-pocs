package mytasks.delayed

import simpletask.SimpleTaskPayload
import simpletask.SimpleTaskTemporalConfig
import simpletask.StartSimpleTaskParams

fun main() {
  val task = DelayedTask()
  task.buildSimpleTask(
    SimpleTaskTemporalConfig(
      temporalTargetEndpoint = "localhost:7233",
      temporalNamespace = "default",
      taskQueue = "delayed-task-queue",
    )
  )

  task.spinUpSimpleTaskWorker()

  try {
    val payload = SimpleTaskPayload.Builder<String>()
      .withTaskPayload("test")
      .withDelayMilliseconds(3000)
      .withVersion("2")
      .build()

    val params = StartSimpleTaskParams(
      payload = payload,
      workflowId = "delayed-task-test",
      resultClass = String::class,
    )

    task.start(params)

    Thread.sleep(7000)

  } finally {
    task.shutdown()
  }
}