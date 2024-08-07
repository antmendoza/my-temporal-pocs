import { Worker } from '@temporalio/worker';
import * as activities from './activities';
import { newContextActivityInterceptor } from './context/activity-interceptors';
import {WorkflowInterceptorsFactory} from "@temporalio/workflow";
import {NodeSDK} from "@opentelemetry/sdk-node";
import {ConsoleSpanExporter} from "@opentelemetry/sdk-trace-base";
import {Resource} from "@opentelemetry/resources";
import {makeWorkflowExporter, OpenTelemetryActivityInboundInterceptor} from "@temporalio/interceptors-opentelemetry";

async function main() {
  const resource = new Resource({
    'service.name': 'interceptors-sample-worker',
  });

  // Export spans to console for simplicity
  const exporter = new ConsoleSpanExporter();

  const otel = new NodeSDK({ traceExporter: exporter, resource });
  await otel.start();


  // Create a worker that uses the Runtime instance installed above
  const worker = await Worker.create({
    workflowsPath: require.resolve('./workflows'),
    activities,
    taskQueue: 'context-propagation',
    sinks: {
      exporter: makeWorkflowExporter(exporter, resource),
    },
    interceptors: {
      activity: [newContextActivityInterceptor],
      workflowModules: [require.resolve('./context/workflow-interceptors')],
      activityInbound: [(ctx) => new OpenTelemetryActivityInboundInterceptor(ctx)]
    },
  });
  await worker.run();
}



main().then(
  () => void process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  }
);
