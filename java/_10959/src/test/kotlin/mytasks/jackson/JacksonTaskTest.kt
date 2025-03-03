package mytasks.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import io.temporal.common.converter.DefaultDataConverter
import io.temporal.samples.hello.MyPayloadConverter
import org.apache.commons.lang3.SerializationUtils
import org.junit.Test
import simpletask.SimpleTaskPayload
import testing.SimpleTaskTestEnvironment
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.util.Optional
import kotlin.test.assertEquals


class JacksonTaskTest {
    @Test
    fun `test task execution logic returns expected result`() {
        val task = JacksonTask()
        val testEnv = SimpleTaskTestEnvironment(task,
            DefaultDataConverter.STANDARD_INSTANCE)

        val payload = SimpleTaskPayload.Builder<JacksonTaskInput>()
            .withTaskPayload(JacksonTaskInput("Test"))
            .build()

        val result = testEnv.executeSimpleTask(payload, JacksonTaskOutput::class.java, JacksonTaskOutput::class.java)

        assertEquals("Hello Test", result.output )
    }
}
