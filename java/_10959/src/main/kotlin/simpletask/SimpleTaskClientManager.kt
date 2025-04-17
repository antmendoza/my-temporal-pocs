package simpletask

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.client.WorkflowStub
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

class SimpleTaskClientManager(
  private val workflowClient: WorkflowClient,
  private val taskQueue: String,
) {
  fun getWorkflowClient(): WorkflowClient {
    return this.workflowClient
  }

  private fun createDelayedWorkflowStub(
    workflowId: String,
    workflowType: String,
    simpleTaskWorkflowConfig: SimpleTaskWorkflowConfig,
    startDelay: Duration,
    version: String,
  ): WorkflowStub {
    val workflowOptionsBuilder =
      WorkflowOptions.newBuilder()
        .setTaskQueue(taskQueue)
        .setWorkflowId(workflowId)
        .setWorkflowIdReusePolicy(simpleTaskWorkflowConfig.workflowIdReusePolicy)

    if (version != "1") {
      workflowOptionsBuilder.setStartDelay(startDelay.toJavaDuration())
    }
    val workflowOptions = workflowOptionsBuilder.build()

    return workflowClient.newUntypedWorkflowStub(workflowType, workflowOptions)
  }

  private fun createWorkflowStub(
    workflowId: String,
    workflowType: String,
    simpleTaskWorkflowConfig: SimpleTaskWorkflowConfig,
  ): WorkflowStub {
    val workflowOptions =
      WorkflowOptions.newBuilder()
        .setTaskQueue(taskQueue)
        .setWorkflowId(workflowId)
        .setWorkflowIdReusePolicy(simpleTaskWorkflowConfig.workflowIdReusePolicy)
        .build()

    return workflowClient.newUntypedWorkflowStub(workflowType, workflowOptions)
  }

  fun <T> start(
    payload: SimpleTaskPayload<T>,
    workflowId: String,
    workflowType: String,
    simpleTaskWorkflowConfig: SimpleTaskWorkflowConfig,
  ): WorkflowStub {
    val workflowStub = createDelayedWorkflowStub(
      workflowId, workflowType, simpleTaskWorkflowConfig,
      payload.getDelayMilliseconds().toDuration(DurationUnit.MILLISECONDS),
      payload.getVersion()
    )

    workflowStub.start(payload)
    return workflowStub
  }

  fun <T, K : Any> executeTaskAndWaitForResult(
    payload: SimpleTaskPayload<T>,
    workflowId: String,
    workflowType: String,
    resultClass: KClass<K>,
    simpleTaskWorkflowConfig: SimpleTaskWorkflowConfig,
  ): K {
    val workflowStub = start(payload, workflowId, workflowType, simpleTaskWorkflowConfig)
    return workflowStub.getResult(resultClass.java)
  }
}