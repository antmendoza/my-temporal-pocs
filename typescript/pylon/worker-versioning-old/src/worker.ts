import { NativeConnection, Worker } from '@temporalio/worker';
import * as activities from './activities';

const TASK_QUEUE = 'worker-versioning-old-tq';

async function main() {
  const buildId = process.env.BUILD_ID ?? 'v1.0';
  const useVersioning = (process.env.USE_VERSIONING ?? 'true').toLowerCase() === 'true';

  const connection = await NativeConnection.connect({
    address: process.env.TEMPORAL_ADDRESS ?? 'localhost:7233',
  });

  const worker = await Worker.create({
    connection,
    namespace: process.env.TEMPORAL_NAMESPACE ?? 'default',
    taskQueue: TASK_QUEUE,
    workflowsPath: require.resolve('./workflows'),
    activities,
    // Old (Build ID-based) Worker Versioning. Replaced by Worker Deployment
    // Versioning in newer SDKs, but still available in 1.15.
    //buildId,
    //useVersioning,
  });

  console.log(`Worker starting | taskQueue=${TASK_QUEUE} buildId=${buildId} useVersioning=${useVersioning}`);
  await worker.run();
}

main().then(
  () => process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  }
);
