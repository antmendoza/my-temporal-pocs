import {Worker} from '@temporalio/worker';
import * as activities from './activities';

async function run() {
  const worker = await Worker.create({
    workflowsPath: require.resolve('./workflows'),
    activities,
    taskQueue: 'activities-examples',
//    maxCachedWorkflows RAM
//    maxConcurrentWorkflowTaskExecutions CPU
//    maxConcurrentLocalActivityExecutions CPU
//    maxConcurrentActivityTaskExecutions CPU
  });

  await worker.run();
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
