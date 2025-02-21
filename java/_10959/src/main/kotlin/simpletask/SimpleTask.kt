package simpletask

import io.temporal.activity.DynamicActivity
import io.temporal.client.WorkflowOptions
import io.temporal.client.WorkflowStub
import io.temporal.common.converter.EncodedValues
import io.temporal.workflow.DynamicWorkflow
import java.util.UUID
import kotlin.reflect.KClass

abstract class SimpleTask<T, K: Any> {
  protected lateinit var simpleTaskWorkerManager: SimpleTaskWorkerManager
  protected lateinit var simpleTaskClientManager: SimpleTaskClientManager
  protected lateinit var namespace: String

  fun buildSimpleTask(simpleTaskTemporalConfig: SimpleTaskTemporalConfig) {
    simpleTaskClientManager = SimpleTaskResourceBuilder.buildTemporalClient(
      temporalTargetEndpoint = simpleTaskTemporalConfig.temporalTargetEndpoint,
      temporalNamespace = simpleTaskTemporalConfig.temporalNamespace,
      taskQueue = simpleTaskTemporalConfig.taskQueue,
      dataConverter = simpleTaskTemporalConfig.dataConverter,
      serviceStubsOptions = simpleTaskTemporalConfig.serviceStubOptions,
      simpleTaskName = getSimpleTaskName(),
    )
    simpleTaskWorkerManager =
      SimpleTaskResourceBuilder.buildTemporalWorkerFactory(
        workflowClient = simpleTaskClientManager.getWorkflowClient(),
        taskQueue = simpleTaskTemporalConfig.taskQueue,
        workflowClass = getWorkflowClass(),
        activityInstance = CashActivity(),
        workerOptions = simpleTaskTemporalConfig.workerOptions,
      )
    namespace = simpleTaskTemporalConfig.temporalNamespace
  }

  open fun getSimpleTaskName(): String {
    return this.javaClass.name
  }

  fun spinUpSimpleTaskWorker() {
    // This starts the workerFactory and the workers which have been registered to it
    this.simpleTaskWorkerManager.startup()
  }

  abstract fun taskExecutionLogic(taskPayload: SimpleTaskPayload<T>): K

  inner class CashActivity : DynamicActivity {
    override fun execute(encodedValues: EncodedValues): K {
      @Suppress("UNCHECKED_CAST")
      val simpleTaskPayload =
        encodedValues.get(SimpleTaskPayload::class.java) as SimpleTaskPayload<T>

      return taskExecutionLogic(simpleTaskPayload)
    }
  }

  open fun getWorkflowClass(): Class<out DynamicWorkflow> {
    return SimpleWorkflow::class.java
  }

  fun shutdown() {
    this.simpleTaskWorkerManager.shutdown()
  }

  fun executeTaskAndWaitForResult(params: StartSimpleTaskParams<T, K>): K {
    return this.simpleTaskClientManager.executeTaskAndWaitForResult(
      params.payload,
      generateWorkflowId(params.workflowId),
      generateWorkflowType(params.workflowType),
      params.resultClass,
      params.simpleTaskWorkflowConfig,
    )
  }

  fun generateWorkflowType(workflowType: String?): String {
    if (workflowType != null) return workflowType

    return this::class.java.simpleName
  }

  companion object {
    @JvmStatic
    protected fun generateWorkflowId(workflowId: String?): String {
      if (workflowId != null) return workflowId

      return UUID.randomUUID().toString()
    }
  }
}