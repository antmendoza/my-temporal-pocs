// @@@SNIPSTART typescript-hello-client
import { Connection, Client } from '@temporalio/client';
import {exampleFailedActivity, exampleParentChild} from './workflows';
import { nanoid } from 'nanoid';
import {WorkflowHandleWithFirstExecutionRunId} from "@temporalio/client/src/workflow-client";

async function run() {
  const connection = await Connection.connect({ address: 'localhost:7233' });

  const client = new Client({
    connection,
  });


  for (let i = 0; i <200; i++) {

    const handle = await client.workflow.start(exampleFailedActivity, {
      taskQueue: 'hello-world',
      args: ['Temporal'],
      workflowId: 'workflow-' + nanoid(),
    });
    console.log(`Started workflow ${handle.workflowId}`);

  }

  for (let i = 0; i <200; i++) {

    const handle = await client.workflow.start(exampleParentChild, {
      taskQueue: 'hello-world',
      args: ['Temporal'],
      workflowId: 'workflow-' + nanoid(),
    });

    console.log(`Started workflow ${handle.workflowId}`);

  }


}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
