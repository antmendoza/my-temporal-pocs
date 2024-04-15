import io.temporal.workflow.Workflow
import java.time.Duration

class Workflow_5611Impl : Workflow_5611 {

    private val signalValue = false

    override fun run(): String {

        val awaitSignal = Workflow.await(Duration.ofSeconds(5)) { signalValue }

        if (!awaitSignal){
            println("timer fired")
        }

        return "done";
    }

}
