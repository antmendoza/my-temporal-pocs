import { Client, Connection } from '@temporalio/client';
import { greetingWorkflow } from './workflows';

const TASK_QUEUE = 'worker-versioning-old-tq';

async function main() {
  const connection = await Connection.connect({
    address: process.env.TEMPORAL_ADDRESS ?? 'localhost:7233',
  });
  const client = new Client({
    connection,
    namespace: process.env.TEMPORAL_NAMESPACE ?? 'default',
  });

  const handle = await client.workflow.start(greetingWorkflow, {
    args: ['World'],
    taskQueue: TASK_QUEUE,
    workflowId: `greeting-${Date.now()}`,
  });

  console.log(`Started workflow ${handle.workflowId}`);
  const result = await handle.result();
  console.log(`Result: ${result}`);
}

main().then(
  () => process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  }
);
