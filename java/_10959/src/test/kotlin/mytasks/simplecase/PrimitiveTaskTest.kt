package mytasks.simplecase

import io.temporal.common.converter.DefaultDataConverter
import junit.framework.TestCase
import mytasks.jackson.JacksonTask
import mytasks.jackson.JacksonTaskInput
import org.junit.Assert
import org.junit.Test
import simpletask.SimpleTaskPayload
import testing.SimpleTaskTestEnvironment

class PrimitiveTaskTest {
  @Test
  fun `test task execution logic returns expected result`() {
    val task = PrimitiveTask()
    val testEnv = SimpleTaskTestEnvironment(task, DefaultDataConverter.STANDARD_INSTANCE)

    val payload = SimpleTaskPayload.Builder<String>()
      .withTaskPayload("Test")
      .build()

    val result = testEnv.taskStub().taskExecutionLogic(payload)

    Assert.assertEquals("Hello Test", result)
  }
}