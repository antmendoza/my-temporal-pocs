import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class Workflow_5611ImplJava implements Workflow_5611 {


    private final boolean signalValue = false;

    private final Activity_5611 activity = Workflow.newActivityStub(Activity_5611.class,
            ActivityOptions.newBuilder().
                    setStartToCloseTimeout(Duration.ofSeconds(20))
                    .build()
    );


    @Override
    public String run() {


        boolean awaitSignal = Workflow.await(Duration.ofSeconds(5), () -> {
            System.out.println("In Workflow.await java");
            activity.doSomething();
            return true;
        });


        if (!awaitSignal) {

            System.out.println(">>>>>>>>>  timer fired");

        }

        return "done";
    }
}




