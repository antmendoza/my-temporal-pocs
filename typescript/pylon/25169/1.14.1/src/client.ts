import { Connection, Client } from '@temporalio/client';
import { loadClientConnectConfig } from '@temporalio/envconfig';
import { example } from './workflows';
import { nanoid } from 'nanoid';

export async function startWorkflow(client: Client) {
  const handle = await client.workflow.start(example, {
    taskQueue: 'hello-world',
    args: ['Temporal'],
    workflowId: 'workflow-',
  });
  console.log(`Started workflow ${handle.workflowId}`);
  return handle;
}

async function run() {
  const config = loadClientConnectConfig();
  const connection = await Connection.connect(config.connectionOptions);
  const client = new Client({ connection });
  await startWorkflow(client);
}

