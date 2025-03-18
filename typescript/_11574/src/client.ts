import { Client } from '@temporalio/client';
import { asyncActivityWorkflow, httpWorkflow } from './workflows';

async function run(): Promise<void> {
  const client = new Client();

  let result = await client.workflow.execute(httpWorkflow, {
    taskQueue: 'activities-examples',
    workflowId: 'activities-examples',
  });
  console.log(result); // 'The answer is 42'

  result = await client.workflow.execute(asyncActivityWorkflow, {
    taskQueue: 'activities-examples',
    workflowId: 'activities-examples',
  });
  console.log(result);
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
