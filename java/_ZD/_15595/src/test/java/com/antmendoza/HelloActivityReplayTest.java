package com.antmendoza;

import io.temporal.activity.LocalActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.common.WorkflowExecutionHistory;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.testing.WorkflowReplayer;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.junit.Rule;
import org.junit.Test;

public class HelloActivityReplayTest {

  @Rule
  public TestWorkflowRule testWorkflowRule =
      TestWorkflowRule.newBuilder()
          // This use temporal service, Temporal has to be running in localhost:7233
          .setUseExternalService(true)
          .setNamespace("default")
          .setDoNotStart(true)
          .build();

  @Test
  public void replayWorkflowExecutionSameImplementation() throws Exception {

    executeWorkflow(HelloActivity.GreetingWorkflowImpl.class);

    String eventHistoryAsString = getEventHistoryAsString();

    // write json to file
    //    Path path = Path.of("src/test/resources/HelloActivityHistory.json");

    // Ensure parent directories exist
    //    Files.createDirectories(path.getParent());

    /*   Files.writeString(
               path,
               eventHistory,
               StandardOpenOption.CREATE,
               StandardOpenOption.TRUNCATE_EXISTING,
               StandardOpenOption.WRITE
       );
    */

    WorkflowReplayer.replayWorkflowExecution(
        eventHistoryAsString, HelloActivity.GreetingWorkflowImpl.class);
  }

  // This test execute the workflow with one implementation (GreetingWorkflowImpl) and
  // replay the even history with a different implementation (GreetingWorkflowImplTest),
  @Test
  public void replayWorkflowExecutionDifferentImplementation() throws Exception {

    executeWorkflow(GreetingWorkflowImplTest.class);

    String eventHistoryAsString = getEventHistoryAsString();

    WorkflowReplayer.replayWorkflowExecution(
        eventHistoryAsString, HelloActivity.GreetingWorkflowImpl.class);
  }

  private String getEventHistoryAsString() {
    return new WorkflowExecutionHistory(
            testWorkflowRule.getHistory(
                WorkflowExecution.newBuilder().setWorkflowId(HelloActivity.WORKFLOW_ID).build()))
        .toJson(true);
  }

  private void executeWorkflow(
      Class<? extends HelloActivity.GreetingWorkflow> workflowImplementationType) {

    testWorkflowRule
        .getWorker()
        .registerActivitiesImplementations(new HelloActivity.GreetingActivitiesImpl());

    testWorkflowRule.getWorker().registerWorkflowImplementationTypes(workflowImplementationType);

    testWorkflowRule.getTestEnvironment().start();

    HelloActivity.GreetingWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                HelloActivity.GreetingWorkflow.class,
                WorkflowOptions.newBuilder()
                    .setWorkflowId(HelloActivity.WORKFLOW_ID)
                    .setTaskQueue(testWorkflowRule.getTaskQueue())
                    .setWorkflowIdReusePolicy(
                        WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_TERMINATE_IF_RUNNING)
                    .build());

    WorkflowStub.fromTyped(workflow).start("Hello");

    // wait until workflow completes
    WorkflowStub.fromTyped(workflow).getResult(String.class);
  }

  public static class GreetingWorkflowImplTest implements HelloActivity.GreetingWorkflow {

    private final HelloActivity.GreetingActivities activities =
        Workflow.newLocalActivityStub(
            HelloActivity.GreetingActivities.class,
            LocalActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(2))

                    //This is the only difference
                .setDoNotIncludeArgumentsIntoMarker(true)


                    .build());

    @Override
    public String getGreeting(String name) {

      // This is a blocking call that returns only after the activity has completed.
      return activities.composeGreeting("Hello", name);
    }
  }
}
