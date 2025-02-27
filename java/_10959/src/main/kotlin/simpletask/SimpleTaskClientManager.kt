package simpletask

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.client.WorkflowStub
import kotlin.reflect.KClass

class SimpleTaskClientManager(
  private val workflowClient: WorkflowClient,
  private val taskQueue: String,
) {
  fun getWorkflowClient(): WorkflowClient {
    return this.workflowClient
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

  fun <T, K : Any> executeTaskAndWaitForResult(
    payload: SimpleTaskPayload<T>,
    workflowId: String,
    workflowType: String,
    resultClass: KClass<K>,
    simpleTaskWorkflowConfig: SimpleTaskWorkflowConfig,
  ): K {
    val workflowStub = createWorkflowStub(workflowId, workflowType, simpleTaskWorkflowConfig)


    //Antonio: this fails with the same class java.util.LinkedHashMap cannot be cast to class mytasks.jackson.JacksonTaskOutput
    //   workflowStub.start(payload)
    //    return workflowStub.getResult(Any::class.java) as K

    workflowStub.start(payload)
    return workflowStub.getResult(resultClass.java)
  }
}