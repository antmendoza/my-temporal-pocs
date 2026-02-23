import { Client } from '@temporalio/client';
import { parentWorkflow } from './workflows';
import { WorkflowIdConflictPolicy } from '@temporalio/common/src/workflow-options';

async function run() {
  const client = new Client();

  const result = await client.workflow.execute(parentWorkflow, {
    taskQueue: 'child-workflows',
    workflowId: 'parent-sample-0',
    args: [''],
    workflowIdConflictPolicy: "TERMINATE_EXISTING",
  });
  console.log(result);
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
