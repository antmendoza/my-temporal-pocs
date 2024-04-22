2024-04-16T13:18:03.645Z [ERROR] Failed to activate workflow {
  namespace: 'default',
  taskQueue: 'test',
  workflowId: 'b8757f6d-1f3c-464c-b1a5-792f76172221',
  runId: '632986f2-0988-46bd-ae7c-618fbadc4622',
  workflowType: 'CreateMissingReplicas',
  error: TypeError: value.getTime is not a function
      at Type.fromObject (/node_modules/@goparrot/vendor-tokens-sdk/cjs/square-token-client/proto/square_token.js:11:33)
      at Type.WorkflowActivation$fromObject [as fromObject] (eval at Codegen (/node_modules/@protobufjs/codegen/index.js:50:33), <anonymous>:13:24)
      at VMWorkflow.activate (/node_modules/@temporalio/worker/lib/workflow/vm-shared.js:313:97)
      at /node_modules/@temporalio/worker/lib/worker.js:802:78
      at async /node_modules/@temporalio/worker/lib/tracing.js:65:20
      at async /node_modules/@temporalio/worker/lib/worker.js:692:28,
  workflowExists: true
}