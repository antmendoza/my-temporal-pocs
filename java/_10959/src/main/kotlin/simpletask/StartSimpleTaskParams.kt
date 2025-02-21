package simpletask

import kotlin.reflect.KClass

data class StartSimpleTaskParams<T, K : Any>(
  val payload: SimpleTaskPayload<T>,
  val workflowType: String? = null,
  val workflowId: String? = null,
  val simpleTaskWorkflowConfig: SimpleTaskWorkflowConfig = SimpleTaskWorkflowConfig.Builder()
    .build(),
  val resultClass: KClass<K>,
)