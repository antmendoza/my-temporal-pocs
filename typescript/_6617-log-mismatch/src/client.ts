
import { Connection, Client } from '@temporalio/client';
import { example } from './workflows';
import { nanoid } from 'nanoid';

async function run() {
  // Connect to the default Server location
  const connection = await Connection.connect({ address: 'localhost:7233' });

  const client = new Client({
    connection,
  });

  for (let i = 0; i < 50000; i++) {
    const handle = await client.workflow.start(example, {
      taskQueue: 'hello-world',
      args: ['Temporal'],
      workflowId: 'workflow-' + nanoid(),
    });
    console.log(`Started workflow ${handle.workflowId}`);

//    await new Promise(r => setTimeout(r, 10));


//    console.log(await handle.result());

  }

}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});

