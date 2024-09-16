package com.antmendoza;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.temporal.client.WorkflowOptions;
import io.temporal.testing.*;
import org.junit.Rule;
import org.junit.Test;

public class HelloActivityTest {

  @Rule
  public TestWorkflowRule testWorkflowRule =
      TestWorkflowRule.newBuilder()
          .setWorkflowTypes(HelloActivity.GreetingWorkflowImpl.class)
          .setDoNotStart(true)
          .build();

  @Test
  public void testActivityImpl() {

    testWorkflowRule
        .getWorker()
        .registerActivitiesImplementations(
            new HelloActivity.GreetingActivitiesImpl(
                new GetAttempt() {
                  @Override
                  public int get() {
                    return 1;
                  }
                }));
    testWorkflowRule.getTestEnvironment().start();

    // Get a workflow stub using the same task queue the worker uses.
    HelloActivity.GreetingWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                HelloActivity.GreetingWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
    // Execute a workflow waiting for it to complete.
    String greeting = workflow.getGreeting("World");
    assertEquals("Hello World!", greeting);

    testWorkflowRule.getTestEnvironment().shutdown();
  }

  @Test
  public void testMockedActivity() {
    // withoutAnnotations() is required to stop Mockito from copying
    // method-level annotations from the GreetingActivities interface
    HelloActivity.GreetingActivities activities =
        mock(HelloActivity.GreetingActivities.class, withSettings().withoutAnnotations());
    when(activities.composeGreeting("Hello", "World")).thenReturn("Hello World!");
    testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
    testWorkflowRule.getTestEnvironment().start();

    // Get a workflow stub using the same task queue the worker uses.
    HelloActivity.GreetingWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(
                HelloActivity.GreetingWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
    // Execute a workflow waiting for it to complete.
    String greeting = workflow.getGreeting("World");
    assertEquals("Hello World!", greeting);

    testWorkflowRule.getTestEnvironment().shutdown();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMockedActivityWithoutSettings() {
    // Mocking activity that has method-level annotations
    // with no withoutAnnotations() setting results in a failure
    HelloActivity.GreetingActivities activities = mock(HelloActivity.GreetingActivities.class);
    testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
  }
}
