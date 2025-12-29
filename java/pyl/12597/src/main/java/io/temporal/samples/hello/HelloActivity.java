package io.temporal.samples.hello;

import com.sun.net.httpserver.HttpServer;
import com.uber.m3.tally.RootScopeBuilder;
import com.uber.m3.tally.Scope;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.reporter.MicrometerClientStatsReporter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.*;
import io.temporal.workflow.unsafe.WorkflowUnsafe;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HelloActivity {

  // Define the task queue name
  static final String TASK_QUEUE = "HelloActivityTaskQueue";

  // Define our workflow unique id
  static final String WORKFLOW_ID = "HelloActivityWorkflow";

  @WorkflowInterface
  public interface GreetingWorkflow {

    @WorkflowMethod
    void run();
  }

  @ActivityInterface
  public interface GreetingActivities {

    @ActivityMethod
    String sleepMs(int ms);
  }

  public static class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities activities =
        Workflow.newLocalActivityStub(
            GreetingActivities.class,
            LocalActivityOptions.newBuilder()
                // arbitrary timeout to allow long sleeps for testing
                .setStartToCloseTimeout(Duration.ofSeconds(20))
                .build());

    @Override
    public void run() {

      if (WorkflowUnsafe.isReplaying()) {
        System.out.println("Workflow replaying");
      } else {
        System.out.println("Workflow starting");
      }

      for (int j = 0; j < 10; j++) {
        final List<Promise<String>> activityFutures = new ArrayList<>();
        int numberOfLocalActivitiesToSchedule = j * 5;
        for (int i = 0; i < numberOfLocalActivitiesToSchedule; i++) {
          int sleepTime = (i * 10);
          activityFutures.add(Async.function(activities::sleepMs, sleepTime));
        }

        if (!WorkflowUnsafe.isReplaying()) {
          System.out.println("Activities scheduled: " + activityFutures.size());
        }

        Promise.allOf(activityFutures).get();

        Workflow.sleep(Duration.ofSeconds(1));
      }

      System.out.println("Workflow continuing as new");
      Workflow.continueAsNew();
    }
  }

  /** Simple activity implementation, that concatenates two strings. */
  public static class GreetingActivitiesImpl implements GreetingActivities {

    @Override
    public String sleepMs(int ms) {

      try {
        Thread.sleep(ms);
      } catch (InterruptedException e) {
        // throw new RuntimeException(e);
      }
      return ms + " ms";
    }
  }

  public static void main(String[] args) {

    int port = 8078;
    Scope scope = startPrometheusEndpoint(port);

    queryLocalActivityMetrics();

    WorkflowServiceStubsOptions serviceStubOptions =
        WorkflowServiceStubsOptions.newBuilder().setMetricsScope(scope).build();

    WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(serviceStubOptions);

    WorkflowClient client =
        WorkflowClient.newInstance(service, WorkflowClientOptions.newBuilder().build());

    startAndRestartWorker(client);

    startWorkflow(client);
  }

  private static void startWorkflow(WorkflowClient client) {
    // Create the workflow client stub. It is used to start our workflow execution.
    GreetingWorkflow workflow =
        client.newWorkflowStub(
            GreetingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                // for testing purpose, to force rescheduling workflow tasks after worker restarts
                .setWorkflowTaskTimeout(Duration.ofSeconds(2))
                .setTaskQueue(TASK_QUEUE)
                .build());

    WorkflowClient.start(workflow::run);
  }

  private static CompletableFuture<Void> startAndRestartWorker(WorkflowClient client) {
    return CompletableFuture.runAsync(
        () -> {
          while (true) {

            WorkerFactory factory = WorkerFactory.newInstance(client);
            // numberOfLocalActivitiesToSchedule is 45
            // generate a random number between 20 and 70 to set max concurrent local activity
            // execution size
            int maxConcurrentLocalActivityExecutionSize = 20 + (int) (Math.random() * 50);

            Worker worker =
                factory.newWorker(
                    TASK_QUEUE,
                    WorkerOptions.newBuilder()
                        .setMaxConcurrentLocalActivityExecutionSize(
                            maxConcurrentLocalActivityExecutionSize)
                        .build());
            worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
            worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
            factory.start();

            System.out.println(
                "worker started with maxConcurrentLocalActivityExecutionSize: "
                    + maxConcurrentLocalActivityExecutionSize);

            try {
              Thread.sleep(4_000);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }

            System.out.println("stopping worker");

            factory.shutdownNow();
          }
        });
  }

  private static void queryLocalActivityMetrics() {
    CompletableFuture.runAsync(
        () -> {
          while (true) {

            String metricFilter = "worker_type=\"LocalActivityWorker\"";
            double temporalLocalActivityTaskSlotsAvailable =
                new QuerySdkMetric("temporal_worker_task_slots_available", metricFilter)
                    .fetchMetric();

            double temporalLocalActivityTaskSlotsUsed =
                new QuerySdkMetric("temporal_worker_task_slots_used", metricFilter).fetchMetric();

            if (temporalLocalActivityTaskSlotsAvailable < 0
                || temporalLocalActivityTaskSlotsUsed < 0) {

              System.out.println(
                  ">>>>>>> \n Negative slots calculation! "
                      + ", temporalLocalActivityTaskSlotsAvailable: "
                      + temporalLocalActivityTaskSlotsAvailable
                      + ", temporalLocalActivityTaskSlotsUsed: "
                      + temporalLocalActivityTaskSlotsUsed);
            }

            try {
              Thread.sleep(200);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        });
  }

  private static Scope startPrometheusEndpoint(int port) {

    System.out.println("Starting Prometheus worker on port " + port);

    // Set up prometheus registry and stats reported
    PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    // Set up a new scope, report every 1 second
    Scope scope =
        new RootScopeBuilder()
            .reporter(new MicrometerClientStatsReporter(registry))
            .reportEvery(com.uber.m3.util.Duration.ofMillis(200));
    // Start the prometheus scrape endpoint for starter metrics
    HttpServer scrapeEndpoint = MetricsUtils.startPrometheusScrapeEndpoint(registry, port);
    // Stopping the starter will stop the http server that exposes the
    // scrape endpoint.
    Runtime.getRuntime().addShutdownHook(new Thread(() -> scrapeEndpoint.stop(1)));
    return scope;
  }
}
