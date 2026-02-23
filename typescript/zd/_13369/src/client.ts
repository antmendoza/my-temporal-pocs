import { Client, Connection } from '@temporalio/client';
import { example } from './workflows';
import { nanoid } from 'nanoid';

async function run() {
  const connection = await Connection.connect({ address: 'localhost:7233' });

  const client = new Client({
    connection,
  });

  const handle = await client.workflow.start(example, {
    taskQueue: 'hello-world',
    args: ['Temporal'],
    workflowId: 'workflow-' + nanoid(),
  });
  console.log(`Started workflow ${handle.workflowId}`);

  await new Promise((f) => setTimeout(f, 5_000));

  await handle.cancel();

  console.log(await handle.result()); // Hello, Temporal!
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
