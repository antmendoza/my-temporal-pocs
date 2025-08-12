package io.temporal.samples.tracing.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class TracingWorkflowImpl implements TracingWorkflow {

  private String language = "English";

  String greet = null;

  @Override
  public String greet(String name) {

    ActivityOptions activityOptions =
        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build();
    TracingActivities activities =
        Workflow.newActivityStub(TracingActivities.class, activityOptions);

    Workflow.sleep(Duration.ofSeconds(10));

    greet = activities.greet(name, language);

    Workflow.sleep(Duration.ofSeconds(5));

    return greet;
  }

  @Override
  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public void updateMethod(String language) {
    Workflow.await(() -> greet != null);
  }

  @Override
  public String getLanguage() {
    return language;
  }
}
