package simpletask

import io.temporal.api.enums.v1.WorkflowIdReusePolicy

class SimpleTaskWorkflowConfig private constructor(
  val workflowIdReusePolicy: WorkflowIdReusePolicy,
) {
  data class Builder(
    var workflowIdReusePolicy: WorkflowIdReusePolicy = WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE,
  ) {
    /**
     * By invoking this with a workflowIdReuse policy you are overriding `WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE`. You should be
     * sure you know what you are doing: [see this](https://docs.temporal.io/workflows#workflow-id-reuse-policy)
     *
     * @param workflowIdReusePolicy
     * @return
     */
    fun withWorkflowIdReusePolicy(workflowIdReusePolicy: WorkflowIdReusePolicy): Builder {
      this.workflowIdReusePolicy = workflowIdReusePolicy
      return this
    }

    fun build(): SimpleTaskWorkflowConfig {
      return SimpleTaskWorkflowConfig(this.workflowIdReusePolicy)
    }
  }
}