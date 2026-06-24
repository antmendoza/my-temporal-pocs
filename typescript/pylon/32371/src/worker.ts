import { Runtime, Worker } from '@temporalio/worker';
import * as activities from './activities';

export const TASK_QUEUE = 'tq_pylon_32371';

async function main() {
  Runtime.install({
    telemetryOptions: {
      metrics: {
        otel: {
          url: 'grpc://localhost:4317',
          metricsExportInterval: '5s',
//          temporality: 'cumulative',
        },
      },
    },
  });

  const worker = await Worker.create({
    workflowsPath: require.resolve('./workflows'),
    activities,
    taskQueue: TASK_QUEUE,
  });

  await worker.run();
}

main().then(
  () => void process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  },
);