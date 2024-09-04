import { Worker } from '@temporalio/worker';

// @@@SNIPSTART typescript-activity-deps-worker
import { createActivities } from './activities';

import WebSocket from 'ws';


async function run() {

  const ws = new WebSocket('ws://localhost:8085');

  const worker = await Worker.create({
    taskQueue: 'dependency-injection',
    workflowsPath: require.resolve('./workflows'),
    activities: createActivities(ws),
  });

  await worker.run();
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
// @@@SNIPEND
