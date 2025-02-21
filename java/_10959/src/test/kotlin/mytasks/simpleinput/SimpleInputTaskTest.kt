package mytasks.simpleinput

import junit.framework.TestCase
import mytasks.dataclass.DataClassTask
import mytasks.dataclass.DataClassTaskInput
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

    val result = testEnv.taskStub().taskExecutionLogic(payload)

    Assert.assertEquals("Hello Test", result.output)
  }
}