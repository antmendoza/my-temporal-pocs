package mytasks.simpleinput

import io.temporal.common.converter.DefaultDataConverter
import junit.framework.TestCase
import mytasks.dataclass.DataClassTask
import mytasks.dataclass.DataClassTaskInput
import mytasks.jackson.JacksonTaskOutput
import org.junit.Assert
import org.junit.Test
import simpletask.SimpleTaskPayload
import simpletask.createWireGsonJsonDataConverter
import testing.SimpleTaskTestEnvironment

class SimpleInputTaskTest {
  @Test
  fun `test task execution logic returns expected result`() {
    val task = SimpleInputTask()
    val testEnv = SimpleTaskTestEnvironment(task, createWireGsonJsonDataConverter())

    val payload = SimpleTaskPayload.Builder<String>()
      .withTaskPayload("Test")
      .build()

    val result = testEnv.executeSimpleTask(payload, SimpleInputTaskOutput::class.java, SimpleInputTaskOutput::class.java)

    Assert.assertEquals("Hello Test", result.output)
  }
}