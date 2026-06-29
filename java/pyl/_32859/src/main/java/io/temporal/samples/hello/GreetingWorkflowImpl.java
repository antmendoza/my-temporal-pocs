package io.temporal.samples.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities activities =
            Workflow.newActivityStub(
                    GreetingActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(10))
                            .build());

    @Override
    public String greet(String name) {
        List<Promise<String>> promises = new ArrayList<>();

        promises.add(Async.function(this::method1));
        promises.add(Async.function(this::method2));



        Promise.allOf(promises).get();


        //errorEventCount(30);

        return activities.composeGreeting(name);
    }

    private static void errorEventCount(int eventCount) {
        if(Workflow.getInfo().getHistoryLength() < eventCount){
            throw new RuntimeException("error"); //failing the workflow task will evict the workflow from the worker cache
        }
    }

    @Override
    public String getGreetingQuery(String name) {
        return "something";
    }

    private String method1() {


        //Workflow.upsertSearchAttributes(Map.of("YourAttributeName", "value"));

        Workflow.getVersion("version1", Workflow.DEFAULT_VERSION, 1);
        activities.composeGreeting("name");


        //Workflow.upsertSearchAttributes(Map.of("YourAttributeName", "value2"));


        activities.composeGreeting("name");


        return "method1";
    }

    private String method2() {

        //Workflow.upsertSearchAttributes(Map.of("YourAttributeName", "value"));
        activities.composeGreeting("name");

        Workflow.getVersion("version2", Workflow.DEFAULT_VERSION, 1);

        //Workflow.upsertSearchAttributes(Map.of("YourAttributeName", "value2"));

        activities.composeGreeting("name");


        return "method1";
    }
}
