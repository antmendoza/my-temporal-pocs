package kotlin

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CustomerCronTask : SimpleTask<Any, CustomerCronTaskResult> {


  /**
   * This is the non-deterministic task logic which the simple task
   * workflow will execute
   *
   * @param taskPayload
   * @return
   */
  override fun taskExecutionLogic(taskPayload: SimpleTaskPayload<Any>): CustomerCronTaskResult {

    return CustomerCronTaskResult(listOf("Riley"))
  }
}

class CustomerCronTaskTest {
  private lateinit var testEnv: SimpleTaskTestEnvironment<Any, CustomerCronTaskResult>

  @Before
  fun setUp() {
    testEnv = SimpleTaskTestEnvironment(CustomerCronTask())
  }

  @Test
  fun `test task execution logic returns expected team members`() {
    val payload = SimpleTaskPayload<Any>("Test");

    val result = testEnv.taskStub().taskExecutionLogic(payload)

    val expectedTeam = listOf("Riley", "Helen", "Dan", "Nick", "Sever")
    assertEquals(expectedTeam, (result as CustomerCronTaskResult).team)
  }
}