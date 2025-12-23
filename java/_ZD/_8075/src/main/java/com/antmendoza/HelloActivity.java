

package com.antmendoza;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.*;
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


    @ActivityMethod
    String composeGreeting(String greeting, String name);
  }

  // Define the workflow implementation which implements our getGreeting workflow method.
  public static class GreetingWorkflowImpl implements GreetingWorkflow {


    private final GreetingActivities activities =
        Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(20)).build());


    public static void main(String[] args) {

      WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();


      WorkflowClient client = WorkflowClient.newInstance(service);


      WorkerFactoryOptions wfo =
              WorkerFactoryOptions.newBuilder()
                      .setWorkerInterceptors(new MyWorkerInterceptor())
                      .validateAndBuildWithDefaults();


      WorkerFactory factory = WorkerFactory.newInstance(client, wfo);


      Worker worker = factory.newWorker(TASK_QUEUE);


      worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);


      worker.registerActivitiesImplementations(new GreetingActivitiesImpl());


      factory.start();

      // Create the workflow client stub. It is used to start our workflow execution.
      GreetingWorkflow workflow =
          client.newWorkflowStub(
              GreetingWorkflow.class,
              WorkflowOptions.newBuilder()
                  .setWorkflowId(WORKFLOW_ID)
                  .setTaskQueue(TASK_QUEUE)
                  .build());


      String greeting = workflow.getGreeting("World");


      System.out.println(greeting);
      System.exit(0);
    }

    @Override
    public String getGreeting(String name) {
      // This is a blocking call that returns only after the activity has completed.
      return activities.composeGreeting("Hello", name);
    }


    /** Simple activity implementation, that concatenates two strings. */
    static class GreetingActivitiesImpl implements GreetingActivities {
      private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

      @Override
      public String composeGreeting(String greeting, String name) {
        log.info("Start Composing greeting...");
        try {
          Thread.sleep(10_000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        log.info("End Composing greeting...");

        return greeting + " " + name + "!";
      }
    }
  }

}
