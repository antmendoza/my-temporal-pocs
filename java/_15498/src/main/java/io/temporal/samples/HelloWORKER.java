package io.temporal.samples;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.config.Client;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import io.temporal.workflow.unsafe.WorkflowUnsafe;
import java.time.Duration;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWORKER {

  static final String TASK_QUEUE = "HelloActivityTaskQueue";

  static final String WORKFLOW_ID = "HelloActivityWorkflow";

  @WorkflowInterface
  public interface GreetingWorkflow {

    @WorkflowMethod
    String getGreeting(io.temporal.samples.proto.Fullname fullName);

    @QueryMethod
    String myQuery();
  }

  @ActivityInterface
  public interface GreetingActivities {

    @ActivityMethod(name = "greet")
    String composeGreeting(String greeting, String name);
  }

  public static class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities activities =
        Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(20)).build());

    @Override
    public String getGreeting(io.temporal.samples.proto.Fullname fullName) {

      Workflow.sleep(Duration.ofSeconds(1));

      // Use the provided protobuf fields to compose a response
      String who = (fullName.getName() + " " + fullName.getSurname()).trim();
      return "hello " + who;
    }

    @Override
    public String myQuery() {
      return "";
    }
  }

  public static class GreetingActivitiesImpl implements GreetingActivities {
    private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

    @Override
    public String composeGreeting(String greeting, String name) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      log.info("Composing greeting...");
      return greeting + " " + name + "!";
    }
  }

  public static void main(String[] args) throws InterruptedException {

    WorkflowClient client = new Client().getWorkflowClient();

    WorkerFactory factory_1 = WorkerFactory.newInstance(client);

    GreetingWorkflow workflow =
        client.newWorkflowStub(
            GreetingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowIdConflictPolicy(
                    WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING)
                .build());

    WorkflowClient.start(
        workflow::getGreeting,
        io.temporal.samples.proto.Fullname.newBuilder()
            .setName("John")
            .setSurname("Smith")
            .build());

    Worker worker_1 =
        factory_1.newWorker(TASK_QUEUE, WorkerOptions.newBuilder()
                .setIdentity("worker1").build());
    worker_1.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
    worker_1.registerActivitiesImplementations(new GreetingActivitiesImpl());
    factory_1.start();

    // to ensure worker 1 get the first workflow task
    Thread.sleep(1_000);

    System.out.println("time before query  " + new Date());
    workflow.myQuery();
    System.out.println("time after query " + new Date());

    Thread.sleep(20_000);
  }
}
