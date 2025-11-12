package io.temporal.samples.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class GreetingWorkflowImpl_withNDE implements GreetingWorkflow {


    private final GreetingActivities activities =
            Workflow.newActivityStub(
                    GreetingActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(10)).build());


    @Override
    public String mainMethod(String name) {


        for (int i = 0; i < 100; i++) {


            Workflow.sleep(Duration.ofSeconds(1));


            activities.sleepSeconds(1);


            if (Workflow.getInfo().isContinueAsNewSuggested()
                    // To avoid long history during testing, we limit the max history length
                    || Workflow.getInfo().getHistoryLength() > 20
            ) {

                break;

            }
        }

        Workflow.continueAsNew(name);

        return "hello";
    }

}
