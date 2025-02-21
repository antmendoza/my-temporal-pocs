package mytasks.dataclass

import io.temporal.common.converter.DefaultDataConverter
import junit.framework.TestCase
import mytasks.jackson.JacksonTask
import mytasks.jackson.JacksonTaskInput
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

    val result = testEnv.taskStub().taskExecutionLogic(payload)

    Assert.assertEquals("Hello Test", result.output)
  }
}