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

            System.out.println("Replaying workflow with SDK version: " + io.temporal.serviceclient.Version.LIBRARY_VERSION);


        }else {
            System.out.println("Running workflow with SDK version: " + io.temporal.serviceclient.Version.LIBRARY_VERSION);

        }

        List<Promise<String>> promises = new ArrayList<>();

        promises.add(Async.function(this::method2));
        promises.add(Async.function(this::method1));

        Promise.allOf(promises).get();

        //errorEventCount(30);

        return activities.IsAdditionalEvidenceCollectionEnabled(200);
    }

    private static void errorEventCount(int eventCount) {
        if (Workflow.getInfo().getHistoryLength() < eventCount) {
            throw new RuntimeException("error"); //failing the workflow task will evict the workflow from the worker cache
        }
    }

    @Override
    public String getGreetingQuery(String name) {
        return "something";
    }

    private String method1() {


  //      Promise p = Async.function(activities::IsAdditionalEvidenceCollectionEnabled, 1000);

        //Workflow.upsertSearchAttributes(Map.of("YourAttributeName", "value"));

        activities.IsAdditionalEvidenceCollectionEnabled(200);

        Workflow.getVersion("version1", Workflow.DEFAULT_VERSION, 1);

        activities.Method3(200);

        //Workflow.upsertSearchAttributes(Map.of("YourAttributeName", "value2"));

//        p.get();

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
