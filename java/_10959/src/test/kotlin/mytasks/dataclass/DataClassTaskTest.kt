package mytasks.dataclass


import org.junit.Assert
import org.junit.Test
import simpletask.SimpleTaskPayload
import simpletask.createWireGsonJsonDataConverter
import testing.SimpleTaskTestEnvironment

class DataClassTaskTest {
  @Test
  fun `test task execution logic returns expected result`() {
    val task = DataClassTask()
    val testEnv = SimpleTaskTestEnvironment(task, createWireGsonJsonDataConverter())

    val payload = SimpleTaskPayload.Builder<DataClassTaskInput>()
      .withTaskPayload(DataClassTaskInput("Test"))
      .build()

    val result = testEnv.executeSimpleTask(payload, DataClassTaskOutput::class.java, DataClassTaskOutput::class.java)

    Assert.assertEquals("Hello Test", result.output)
  }
}