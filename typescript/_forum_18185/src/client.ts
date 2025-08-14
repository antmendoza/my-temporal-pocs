
import { Connection, Client } from '@temporalio/client';
import { runSimpleDAG } from './workflows';
import { nanoid } from 'nanoid';

async function run() {
  // Connect to the default Server location
  const connection = await Connection.connect({ address: 'localhost:7233' });

  const client = new Client({
    connection,
  });

  const handle = await client.workflow.start(runSimpleDAG, {
    taskQueue: 'hello-world',
    workflowId: 'workflow-' + nanoid(),
  });
  console.log(`Started workflow ${handle.workflowId}`);

  // optional: wait for client result
  console.log(await handle.result()); // Hello, Temporal!
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});

