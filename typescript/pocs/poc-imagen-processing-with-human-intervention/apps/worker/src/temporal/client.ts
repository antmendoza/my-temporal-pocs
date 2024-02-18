import { Connection, Client } from '@temporalio/client';
import { processImages } from './workflows';

async function run() {
  const connection = await Connection.connect({ address: 'localhost:7233' });

  const client = new Client({
    connection,
  });

  const values = Array.from({ length: 20 }, (x, i) => i).map((r) => {
    return {
      name: 'img' + r,
    };
  });

  const handle = await client.workflow.start(processImages, {
    taskQueue: 'image_processing-taskqueue',
    // type inference works! args: [name: string]
    args: [{ images: values }],
    workflowId: 'workflow-' + Math.random(),
  });
  console.log(`Started workflow ${handle.workflowId}`);

  // optional: wait for client result
  console.log(await handle.result()); // Hello, Temporal!
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
// @@@SNIPEND
