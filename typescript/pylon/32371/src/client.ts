import { Client, Connection } from '@temporalio/client';
import { example } from './workflows';
import { TASK_QUEUE } from './worker';

async function run() {
  const connection = await Connection.connect();
  const client = new Client({ connection });

  const result = await client.workflow.execute(example, {
    taskQueue: TASK_QUEUE,
    workflowId: `pylon-32371-${Date.now()}`,
    args: ['Temporal'],
  });

  console.log(result);
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});