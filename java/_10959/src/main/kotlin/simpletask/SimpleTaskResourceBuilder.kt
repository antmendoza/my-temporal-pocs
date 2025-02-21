package simpletask

import com.uber.m3.tally.RootScopeBuilder
import com.uber.m3.tally.StatsReporter
import com.uber.m3.util.Duration
import io.micrometer.core.instrument.MeterRegistry
import io.temporal.activity.DynamicActivity
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.common.converter.DataConverter
import io.temporal.common.reporter.MicrometerClientStatsReporter
import io.temporal.serviceclient.ServiceStubsOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import io.temporal.worker.WorkerFactory
import io.temporal.worker.WorkerOptions
import io.temporal.workflow.DynamicWorkflow

class SimpleTaskResourceBuilder {
  companion object {

    fun buildTemporalClient(
      temporalTargetEndpoint: String,
      simpleTaskName: String,
      temporalNamespace: String,
      dataConverter: DataConverter,
      serviceStubsOptions: ServiceStubsOptions,
      taskQueue: String,
    ): SimpleTaskClientManager {
      val gRPCStubOptions = WorkflowServiceStubsOptions.newBuilder(serviceStubsOptions)
        .setTarget(temporalTargetEndpoint)
        .build()
      val service = WorkflowServiceStubs.newInstance(gRPCStubOptions)
      val workflowClientOptions = WorkflowClientOptions.newBuilder()
        .setNamespace(temporalNamespace)
        .setDataConverter(dataConverter)
        .build()

      val workflowClient = WorkflowClient.newInstance(
        service,
        workflowClientOptions,
      )

      return SimpleTaskClientManager(
        workflowClient,
        taskQueue,
      )
    }

    fun <T : DynamicWorkflow, K : DynamicActivity> buildTemporalWorkerFactory(
      workflowClient: WorkflowClient,
      taskQueue: String,
      workflowClass: Class<T>,
      activityInstance: K,
      workerOptions: WorkerOptions,
    ): SimpleTaskWorkerManager {
      return buildWorkerFactory(
        workflowClient,
        taskQueue,
        workflowClass,
        activityInstance,
        workerOptions,
      )
    }

    private fun <T : DynamicWorkflow, K : DynamicActivity> buildWorkerFactory(
      workflowClient: WorkflowClient,
      taskQueue: String,
      workflowClass: Class<T>,
      activityInstance: K,
      workerOptions: WorkerOptions,
    ): SimpleTaskWorkerManager {
      val factory = WorkerFactory.newInstance(workflowClient)
      val worker = factory.newWorker(taskQueue, workerOptions)

      worker.registerWorkflowImplementationTypes(workflowClass)
      worker.registerActivitiesImplementations(activityInstance)

      return SimpleTaskWorkerManager(
        factory,
      )
    }
  }
}