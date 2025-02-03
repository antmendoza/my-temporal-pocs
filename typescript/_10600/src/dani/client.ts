import { Client } from '@temporalio/client';
import { mainFlow } from './workflows';

async function run() {
  const client = new Client();

  const result = await client.workflow.execute(mainFlow, {
    taskQueue: 'child-workflows',
    workflowId: 'parent-sample-0',
    args: ['Alice', 'Bob', 'Charlie'],
  });
  console.log(result);
  // I am a child named Alice
  // I am a child named Bob
  // I am a child named Charlie
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
