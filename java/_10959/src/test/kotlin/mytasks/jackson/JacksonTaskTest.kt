package mytasks.jackson

import io.temporal.common.converter.DefaultDataConverter
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Test
import simpletask.SimpleTaskPayload
import testing.SimpleTaskTestEnvironment

class JacksonTaskTest {
  @Test
  fun `test task execution logic returns expected result`() {
    val task = JacksonTask()
    val testEnv = SimpleTaskTestEnvironment(task, DefaultDataConverter.STANDARD_INSTANCE)

    val payload = SimpleTaskPayload.Builder<JacksonTaskInput>()
      .withTaskPayload(JacksonTaskInput("Test"))
      .build()

    val result = testEnv.taskStub().taskExecutionLogic(payload)

    assertEquals("Hello Test", result.output)
  }
}