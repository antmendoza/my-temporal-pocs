import { Connection } from '@temporalio/client';
import { NodeSDK } from '@opentelemetry/sdk-node';
import { DefaultLogger, Runtime } from '@temporalio/worker';
import { getEnv } from './helpers';
import { MetricReader } from '@opentelemetry/sdk-metrics';
import { PrometheusExporter } from '@opentelemetry/exporter-prometheus';
import { metrics } from '@opentelemetry/api';

async function run() {
;

  // After the SDK starts, the global MeterProvider is registered.
  const meter = metrics.getMeter('temporal-metrics');

  // Expose two observable gauges: one for workflow tasks, one for activity tasks.
  const taskQueue = 'interceptors-opentelemetry-example';
  let workflowBacklog = 0;
  let activityBacklog = 0;

  meter
    .createObservableGauge('temporal_workflow_task_backlog', {
      description: 'Approximate backlog count for workflow tasks per task queue',
    })
    .addCallback((observableResult) => {
      observableResult.observe(workflowBacklog, {
        task_queue: taskQueue,
      });
    });

  meter
    .createObservableGauge('temporal_activity_task_backlog', {
      description: 'Approximate backlog count for activity tasks per task queue',
    })
    .addCallback((observableResult) => {
      observableResult.observe(activityBacklog, {
        task_queue: taskQueue,
      });
    });
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

  try {
    // eslint-disable-next-line no-constant-condition
    while (true) {

      // we can use enhanced mode to get stats per task queue type, note that enhance mode is deprecated
      const taskQueueDescWorkflowENHANCED = await connection.workflowService.describeTaskQueue({
        namespace,
        taskQueue: { name: taskQueue },
        reportStats: true,
        reportConfig: true,
       // taskQueueType: 0, //"TASK_QUEUE_TYPE_WORKFLOW"
        apiMode: 1 //ENHANCED: enhance mode is deprecated
      });

      // @ts-ignore
      const workflow_stats = taskQueueDescWorkflowENHANCED.versionsInfo[""].typesInfo[1].stats.approximateBacklogCount  // Workflow tasks
      // @ts-ignore
      const activity_stats = taskQueueDescWorkflowENHANCED.versionsInfo[""].typesInfo[2].stats.approximateBacklogCount  // Activity tasks

      console.log("workflow_stats "+ JSON.stringify(workflow_stats))
      console.log("activity_stats "+ JSON.stringify(activity_stats))


      const TASK_QUEUE_TYPE_WORKFLOW = await connection.workflowService.describeTaskQueue({
        namespace,
        taskQueue: { name: taskQueue },
        taskQueueType: 1, //"TASK_QUEUE_TYPE_WORKFLOW"
        reportStats: true,
        reportConfig: true,
      });

      console.log("TASK_QUEUE_TYPE_WORKFLOW "+ TASK_QUEUE_TYPE_WORKFLOW.stats?.approximateBacklogCount)

      const TASK_QUEUE_TYPE_ACTIVITY = await connection.workflowService.describeTaskQueue({
        namespace,
        taskQueue: { name: taskQueue },
        reportStats: true,
        taskQueueType: 2, //"TASK_QUEUE_TYPE_ACTIVITY"
        reportConfig: true,
      });

      console.log("TASK_QUEUE_TYPE_ACTIVITY "+ TASK_QUEUE_TYPE_ACTIVITY.stats?.approximateBacklogCount)
      console.log("--------- ")








      // Sleep 1 second
      await new Promise((resolve) => setTimeout(resolve, 2000));
    }
  } finally {
  }
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
