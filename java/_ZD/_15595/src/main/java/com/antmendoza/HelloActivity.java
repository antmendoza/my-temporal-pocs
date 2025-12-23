
package com.antmendoza;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloActivity {

  static final String TASK_QUEUE = "HelloActivityTaskQueue";

  static final String WORKFLOW_ID = "HelloActivityWorkflow";

  @WorkflowInterface
  public interface GreetingWorkflow {

    @WorkflowMethod
    String getGreeting(String name);
  }

  @ActivityInterface
  public interface GreetingActivities {

    // Define your activity method which can be called during workflow execution
    @ActivityMethod
    String composeGreeting(String greeting, String name);
  }

  public static class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities activities =
        Workflow.newLocalActivityStub(
            GreetingActivities.class,
            LocalActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(2))
                .build());

    @Override
    public String getGreeting(String name) {

      // This is a blocking call that returns only after the activity has completed.
      return activities.composeGreeting("Hello", name);
    }
  }

  static class GreetingActivitiesImpl implements GreetingActivities {
    private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

    @Override
    public String composeGreeting(String greeting, String name) {
      log.info("Composing greeting...");
      return greeting + " " + name + "!";
    }
  }
}
