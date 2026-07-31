package io.temporal.samples.asyncactivityfanout

import com.uber.m3.tally.RootScopeBuilder
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityOptions
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.client.WorkflowStub
import io.temporal.common.reporter.MicrometerClientStatsReporter
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import io.temporal.worker.MetricsType
import io.temporal.worker.WorkerFactory
import io.temporal.worker.WorkerFactoryOptions
import io.temporal.workflow.Async
import io.temporal.workflow.Promise
import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import java.time.Duration
import kotlin.math.max

object AsyncActivityFanOutComparisonKotlin {
  const val TASK_QUEUE: String = "AsyncActivityFanOutComparisonTaskQueue"

  const val WINDOW_COUNT: Int = 100

  val ACTIVITY_DURATION: Duration = Duration.ofSeconds(5)

  fun activeThreadCount(registry: MeterRegistry): Double {
    var peak = 0.0
    for (g in registry.find(MetricsType.WORKFLOW_ACTIVE_THREAD_COUNT).gauges()) {
      peak = max(peak, g.value())
    }
    return peak
  }

  @Throws(Exception::class)
  @JvmStatic
  fun main(args: Array<String>) {

    val registry = SimpleMeterRegistry()
    val scope =
      RootScopeBuilder()
        .reporter(MicrometerClientStatsReporter(registry))

        // Flush frequently so the gauge tracks the fan-out window closely.
        .reportEvery(com.uber.m3.util.Duration.ofMillis(100.0))

    val target = System.getenv().getOrDefault("TEMPORAL_ADDRESS", "127.0.0.1:7233")
    val service =
      WorkflowServiceStubs.newServiceStubs(
        WorkflowServiceStubsOptions.newBuilder().setTarget(target).setMetricsScope(scope).build()
      )
    val client = WorkflowClient.newInstance(service)

    val factoryOptions =
      WorkerFactoryOptions.newBuilder()
        .build()
    val factory = WorkerFactory.newInstance(client, factoryOptions)
    val worker = factory.newWorker(TASK_QUEUE)
    worker.registerWorkflowImplementationTypes(FanOutWorkflowImpl::class.java)
    worker.registerActivitiesImplementations(WindowActivitiesImpl())
    factory.start()

    val path1Peak = runOnce(client, registry, true)
    val path2Peak = runOnce(client, registry, false)

    println()
    println("=== AsyncActivityFanOutComparisonKotlin Async fan-out of $WINDOW_COUNT activities ===")
    System.out.printf(
      "PATH 1 (stub method reference)  temporal_workflow_active_thread_count peak: %.0f%n", path1Peak
    )
    System.out.printf(
      "PATH 2 (lambda wrapper)         temporal_workflow_active_thread_count peak: %.0f%n", path2Peak
    )

    System.exit(0)
  }

  /** Starts the workflow, polls the gauge while it runs, and returns the peak value seen. */
  @Throws(Exception::class)
  private fun runOnce(
    client: WorkflowClient,
    registry: MeterRegistry,
    useStubReference: Boolean
  ): Double {
    val workflow =
      client.newWorkflowStub(
        FanOutWorkflow::class.java,
        WorkflowOptions.newBuilder()
          .setWorkflowId("AsyncActivityFanOut-" + (if (useStubReference) "path1" else "path2"))
          .setTaskQueue(TASK_QUEUE)
          .build()
      )

    WorkflowClient.start(
      { windowCount: Int, useStub: Boolean -> workflow.processWindows(windowCount, useStub) },
      WINDOW_COUNT,
      useStubReference
    )
    val done = WorkflowStub.fromTyped(workflow).getResultAsync(Void::class.java)

    var peak = 0.0
    while (!done.isDone) {
      peak = max(peak, activeThreadCount(registry))
      Thread.sleep(50)
    }
    done.get() // propagate workflow failures
    return peak
  }

  @WorkflowInterface
  interface FanOutWorkflow {

    @WorkflowMethod fun processWindows(windowCount: Int, useStubReference: Boolean)
  }

  @ActivityInterface
  interface WindowActivities {
    fun processWindow(windowIndex: Int): String
  }

  class FanOutWorkflowImpl : FanOutWorkflow {
    private val activities: WindowActivities =
      Workflow.newActivityStub(
        WindowActivities::class.java,
        ActivityOptions.newBuilder().setStartToCloseTimeout(ACTIVITY_DURATION.plusSeconds(10)).build()
      )

    override fun processWindows(windowCount: Int, useStubReference: Boolean) {
      val promises: MutableList<Promise<String>> = ArrayList(windowCount)

      if (useStubReference) {
        // PATH 1: stub method reference -> intended to be inline
        for (i in 0 until windowCount) {
          promises.add(Async.function(activities::processWindow, i))
        }
      } else {
        // PATH 2: lambda wrapper -> one WorkflowThread spawned per call
        for (i in 0 until windowCount) {
          val idx = i
          promises.add(Async.function { activities.processWindow(idx) })
        }
      }

      Promise.allOf(promises).get()
    }
  }

  internal class WindowActivitiesImpl : WindowActivities {
    override fun processWindow(windowIndex: Int): String {
      try {
        Thread.sleep(ACTIVITY_DURATION.toMillis())
      } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
      }
      return "processed window $windowIndex"
    }
  }
}
