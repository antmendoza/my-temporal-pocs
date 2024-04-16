import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

class Workflow_5611Impl : Workflow_5611 {

    private val signalValue = false

    private val activity = Workflow.newActivityStub(
        Activity_5611::class.java,
        ActivityOptions.newBuilder().
        setStartToCloseTimeout(Duration.ofSeconds(20))
            .build()
    )

    override fun run(): String {


//        val awaitSignal = Workflow.await(Duration.ofSeconds(5)) { signalValue }
        val awaitSignal = Workflow.await(Duration.ofSeconds(5)) {
            println("In Workflow.await")
            activity.doSomething()
            signalValue
        }



        if (!awaitSignal) {
            println("timer fired")
        }

        return "done"
    }

}
