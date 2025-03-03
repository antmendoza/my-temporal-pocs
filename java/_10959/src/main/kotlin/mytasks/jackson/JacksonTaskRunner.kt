package mytasks.jackson

import io.temporal.samples.hello.HelloActivityRunner.dataConverter
import simpletask.SimpleTaskPayload
import simpletask.SimpleTaskTemporalConfig
import simpletask.StartSimpleTaskParams

fun main() {
  val task = JacksonTask()
  task.buildSimpleTask(
    SimpleTaskTemporalConfig(
      temporalTargetEndpoint = "localhost:7233",
      temporalNamespace = "default",
      taskQueue = "jackson-task-queue",
    )
  )

  task.spinUpSimpleTaskWorker()

  try {
    val payload = SimpleTaskPayload.Builder<JacksonTaskInput>()
      .withTaskPayload(JacksonTaskInput("test"))
      .build()

    val params = StartSimpleTaskParams(
      payload = payload,
      workflowId = "jackson-task-test",
      resultClass = JacksonTaskOutput::class
    )

    val result = task.executeTaskAndWaitForResult(params)


    println("Task returned: ${result.output}")
  } finally {
    task.shutdown()
  }
}