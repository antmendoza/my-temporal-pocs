import { Connection } from '@temporalio/client';
import { NodeSDK } from '@opentelemetry/sdk-node';
import { DefaultLogger, Runtime } from '@temporalio/worker';
import { getEnv } from './helpers';
import { MetricReader } from '@opentelemetry/sdk-metrics';
import { PrometheusExporter } from '@opentelemetry/exporter-prometheus';

async function run() {
  Runtime.install({
    logger: new DefaultLogger('WARN'),

    telemetryOptions: {
      metrics: {
        prometheus: {
          bindAddress: '127.0.0.1:9093',
        },
      },
    },
  });

  const metricReader: MetricReader = new PrometheusExporter({
    host: '127.0.0.1',
    port: 9094,
  });

  const otel = new NodeSDK({
    // @ts-ignore
    metricReader,
  });

  await otel.start();
  // Connect to localhost with default ConnectionOptions,
  // pass options to the Connection constructor to configure TLS and other settings.

  const { address, namespace, serverNameOverride, serverRootCACertificate, clientCert, clientKey } = await getEnv();

  const connection = await Connection.connect({
    address,
    tls: {
      serverNameOverride,
      serverRootCACertificate,
      clientCertPair: clientKey &&
        clientCert && {
          crt: clientCert,
          key: clientKey,
        },
    },
    metadata: {
      'temporal-namespace': namespace,
    },
  });

  const taskQueue = 'interceptors-opentelemetry-example';
  try {
    // eslint-disable-next-line no-constant-condition
    while (true) {
      const taskQueueDescWorkflow = await connection.workflowService.describeTaskQueue({
        namespace,
        taskQueue: { name: taskQueue },
        includeTaskQueueStatus: true,
        reportStats: true,
        reportConfig: true,
        taskQueueType: 0, //"TASK_QUEUE_TYPE_WORKFLOW"
      });

      console.log('Workflow :' + taskQueueDescWorkflow.stats?.approximateBacklogCount);

      const taskQueueDescActivity = await connection.workflowService.describeTaskQueue({
        namespace,
        taskQueue: { name: taskQueue },
        includeTaskQueueStatus: true,
        reportStats: true,
        reportConfig: true,
        taskQueueType: 1, //"TASK_QUEUE_TYPE_ACTIVITY"
      });

      console.log('Activity :' + taskQueueDescActivity.stats?.approximateBacklogCount);

      // Sleep 1 second
      await new Promise((resolve) => setTimeout(resolve, 2000));
    }
  } finally {
    await otel.shutdown();
  }
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
