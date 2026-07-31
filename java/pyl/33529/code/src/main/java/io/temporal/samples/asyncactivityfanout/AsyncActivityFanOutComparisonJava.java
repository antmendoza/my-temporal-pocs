package io.temporal.samples.asyncactivityfanout;

import com.uber.m3.tally.RootScopeBuilder;
import com.uber.m3.tally.Scope;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.common.reporter.MicrometerClientStatsReporter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.MetricsType;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.workflow.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class AsyncActivityFanOutComparisonJava {

  static final String TASK_QUEUE = "AsyncActivityFanOutComparisonTaskQueue";

  static final int WINDOW_COUNT = 100;

  static final Duration ACTIVITY_DURATION = Duration.ofSeconds(5);

  @WorkflowInterface
  public interface FanOutWorkflow {
    @WorkflowMethod
    void processWindows(int windowCount, boolean useStubReference);
  }

  @ActivityInterface
  public interface WindowActivities {
    String processWindow(int windowIndex);
  }

  public static class FanOutWorkflowImpl implements FanOutWorkflow {

    private final WindowActivities activities =
        Workflow.newActivityStub(
            WindowActivities.class,
            ActivityOptions.newBuilder()
                .setStartToCloseTimeout(ACTIVITY_DURATION.plusSeconds(10))
                .build());

    @Override
    public void processWindows(int windowCount, boolean useStubReference) {
      List<Promise<String>> promises = new ArrayList<>(windowCount);

      if (useStubReference) {
        // PATH 1: stub method reference → inline, no thread spawned per activity.
        for (int i = 0; i < windowCount; i++) {
          promises.add(Async.function(activities::processWindow, i));
        }
      } else {
        // PATH 2: lambda wrapper → one WorkflowThread spawned per call
        for (int i = 0; i < windowCount; i++) {
          final int idx = i;
          promises.add(Async.function(() -> activities.processWindow(idx)));
        }
      }

      Promise.allOf(promises).get();
    }
  }

  static class WindowActivitiesImpl implements WindowActivities {
    @Override
    public String processWindow(int windowIndex) {
      try {

        LocalActivityOptions.newBuilder().setDoNotIncludeArgumentsIntoMarker(true).build();
        Thread.sleep(ACTIVITY_DURATION.toMillis());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return "processed window " + windowIndex;
    }
  }

  static double activeThreadCount(MeterRegistry registry) {
    double max = 0;
    for (Gauge g : registry.find(MetricsType.WORKFLOW_ACTIVE_THREAD_COUNT).gauges()) {
      max = Math.max(max, g.value());
    }
    return max;
  }

  public static void main(String[] args) throws Exception {

    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    Scope scope =
        new RootScopeBuilder()
            .reporter(new MicrometerClientStatsReporter(registry))
            // Flush frequently so the gauge tracks the fan-out window closely.
            .reportEvery(com.uber.m3.util.Duration.ofMillis(100));

    String target = System.getenv().getOrDefault("TEMPORAL_ADDRESS", "127.0.0.1:7233");
    WorkflowServiceStubs service =
        WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder().setTarget(target).setMetricsScope(scope).build());
    WorkflowClient client = WorkflowClient.newInstance(service);

    WorkerFactoryOptions factoryOptions =
        WorkerFactoryOptions.newBuilder()
            .build();
    WorkerFactory factory = WorkerFactory.newInstance(client, factoryOptions);
    Worker worker = factory.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(FanOutWorkflowImpl.class);
    worker.registerActivitiesImplementations(new WindowActivitiesImpl());
    factory.start();

    double path1Peak = runOnce(client, registry, true);
    double path2Peak = runOnce(client, registry, false);

    System.out.println();
    System.out.println("=== AsyncActivityFanOutComparisonJava Async fan-out of " + WINDOW_COUNT + " activities (Java) ===");
    System.out.printf(
        "PATH 1 (stub method reference)  temporal_workflow_active_thread_count peak: %.0f%n",
        path1Peak);
    System.out.printf(
        "PATH 2 (lambda wrapper)         temporal_workflow_active_thread_count peak: %.0f%n",
        path2Peak);

    System.exit(0);
  }

  /** Starts the workflow, polls the gauge while it runs, and returns the peak value seen. */
  private static double runOnce(
      WorkflowClient client, MeterRegistry registry, boolean useStubReference) throws Exception {
    FanOutWorkflow workflow =
        client.newWorkflowStub(
            FanOutWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId("AsyncActivityFanOut-java-" + (useStubReference ? "path1" : "path2"))
                .setTaskQueue(TASK_QUEUE)
                .build());

    WorkflowClient.start(workflow::processWindows, WINDOW_COUNT, useStubReference);
    CompletableFuture<Void> done = WorkflowStub.fromTyped(workflow).getResultAsync(Void.class);

    double peak = 0;
    while (!done.isDone()) {
      peak = Math.max(peak, activeThreadCount(registry));
      Thread.sleep(50);
    }
    done.get(); // propagate workflow failures
    return peak;
  }
}
