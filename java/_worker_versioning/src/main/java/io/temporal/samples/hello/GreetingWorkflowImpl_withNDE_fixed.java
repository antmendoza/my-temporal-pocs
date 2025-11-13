package io.temporal.samples.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class GreetingWorkflowImpl_withNDE_fixed implements GreetingWorkflow {


    private final GreetingActivities activities =
            Workflow.newActivityStub(
                    GreetingActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(10)).build());


    @Override
    public String mainMethod(String name) {


        while (true) {

            Workflow.sleep(Duration.ofSeconds(1));

            activities.sleepSeconds(1);

            if (Workflow.getInfo().isContinueAsNewSuggested()
                    // To avoid long history during testing, we limit the max history length
                    || Workflow.getInfo().getHistoryLength() > 20
            ) {

                // https://stackoverflow.com/questions/74143884/how-to-use-cadence-temporal-versioning-api-workflow-getversion-in-a-loop
                String changeId = "GreetingWorkflow_withNDE_Versioning" + Workflow.getInfo().getHistoryLength();
                // workflow versioning
                int version = Workflow.getVersion(changeId,
                        Workflow.DEFAULT_VERSION, 1);
                if (version == 1) {
                    //break;
                    Workflow.continueAsNew(name);
                }
            }


        }


        //Workflow.continueAsNew(name);
        //return "hello";
    }

}
