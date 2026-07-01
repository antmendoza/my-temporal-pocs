package io.temporal.samples.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities activities =
            Workflow.newActivityStub(
                    GreetingActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(10))
                            .build());

    @Override
    public String greet(String name) {

        if(WorkflowUnsafe.isReplaying()) {
            System.out.println("wokflow log: Replaying workflow with SDK version: " + io.temporal.serviceclient.Version.LIBRARY_VERSION);
        }else {
            System.out.println("wokflow log: Running workflow with SDK version: " + io.temporal.serviceclient.Version.LIBRARY_VERSION);

        }

        List<Promise<String>> promises = new ArrayList<>();

        promises.add(Async.function(this::method2));
        promises.add(Async.function(this::method1));

        Promise.allOf(promises).get();


        return activities.Method3(200);
    }


    @Override
    public String getGreetingQuery(String name) {
        return "something";
    }

    private String method1() {


        Workflow.getVersion("version1", Workflow.DEFAULT_VERSION, 1);



        activities.Method3(200);




        return "method1";
    }

    private String method2() {


        Workflow.getVersion("version2", Workflow.DEFAULT_VERSION, 1);

        //Workflow.upsertSearchAttributes(Map.of("YourAttributeName", "value"));
        activities.Method2(200);
        //Workflow.upsertSearchAttributes(Map.of("YourAttributeName", "value2"));


        return "method1";
    }
}
