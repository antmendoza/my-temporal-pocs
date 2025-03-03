package simpletask

import io.temporal.activity.ActivityOptions
import io.temporal.common.converter.EncodedValues
import io.temporal.workflow.ActivityStub
import io.temporal.workflow.DynamicWorkflow
import io.temporal.workflow.Workflow
import mytasks.jackson.JacksonTaskOutput
import java.time.Duration

class SimpleWorkflow : DynamicWorkflow {
    private val logger = Workflow.getLogger(SimpleWorkflow::class.java)

    /**
     * Returns a configured activity stub to be invoked by the dynamic workflow
     *
     * @param simpleTaskTimeoutConfig
     * @param simpleTaskRetryConfig
     * @return
     */
    private fun getDynamicActivityStub(
    ): ActivityStub {
        return Workflow.newUntypedActivityStub(
            ActivityOptions
                .newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(10))
                .build()
        )
    }


    /**
     *
     * @param encodedValues
     * @return
     */
    override fun execute(encodedValues: EncodedValues): Any {
        val cashPayload = encodedValues.get(SimpleTaskPayload::class.java)
        var isExecutionBlocked = false
        var shouldContinueAsNew = false

        val activityStub = getDynamicActivityStub()

        // Get the activity result by executing the activity code
        // Param 1 is the name of the activity
        // Param 2 is the return type of the activity
        // Param 3 is the input to the activity
        val activityResult = activityStub.execute(
            cashPayload.getActivityName(),
            Any::class.java,
            cashPayload,
        )

        ///Antonio///
        if (false) {
            logger.info("activityResult toString" + activityResult)
            logger.info("activityResult::class" + activityResult::class)


            try {
                var myOutput = activityResult as JacksonTaskOutput
                logger.info("myOutput.output" + myOutput.output)
            } catch (e: Exception) {
                logger.error("Error executing activity: ${e.message}")
                throw e
            }
        }
        ///Antonio///

        return activityResult
    }
}