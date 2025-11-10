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

            Workflow.getTypedSearchAttributes()
                    .getUntypedValues().keySet().forEach(v ->

                    {
                        String name1 = v.getName();
                        Workflow.getLogger(GreetingWorkflowImpl_withNDE_fixed.class).info("GreetingWorkflowImpl \n " +
                                name1 + " " + Workflow.getSearchAttributeValues(name1));
                    });


            Workflow.sleep(Duration.ofSeconds(1));


            activities.sleepSeconds(1);


            if (Workflow.getInfo().isContinueAsNewSuggested()
                    // To avoid long history during testing, we limit the max history length
                    || Workflow.getInfo().getHistoryLength() > 20
            ) {

                int version = Workflow.getVersion("GreetingWorkflow_withNDE_Versioning" + Workflow.getInfo().getHistoryLength(),
                        Workflow.DEFAULT_VERSION, 1);
                if (version == 1) {
                    break;
                }
            }


        }

        Workflow.continueAsNew(name);

        return "hello";
    }

}
