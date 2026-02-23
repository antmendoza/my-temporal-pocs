import { DefaultLogger, Worker, Runtime } from '@temporalio/worker';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { ConsoleSpanExporter } from '@opentelemetry/sdk-trace-base';
import { NodeSDK } from '@opentelemetry/sdk-node';
import {
  OpenTelemetryActivityInboundInterceptor,
  makeWorkflowExporter,
} from '@temporalio/interceptors-opentelemetry/lib/worker';
import * as activities from './activities';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-proto';

async function main() {
  const resource = new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'interceptors-sample-worker',
  });

  Runtime.install({
    telemetryOptions: {
      metrics: {
        otel: {
          url:"grpc://0.0.0.0:4317"
        }
     },
    },
  });

  // Export spans to console for simplicity
  const exporter = new OTLPTraceExporter();

  const otel = new NodeSDK({
    traceExporter: exporter,
  });

  await otel.start();


  const worker = await Worker.create({
    workflowsPath: require.resolve('./workflows'),
    activities,
    taskQueue: 'interceptors-opentelemetry-example',
    sinks: {
      exporter: makeWorkflowExporter(exporter, resource),
    },
    // Registers opentelemetry interceptors for Workflow and Activity calls
    interceptors: {
      // example contains both workflow and interceptors
      workflowModules: [require.resolve('./workflows')],
      activity: [(ctx) => ({ inbound: new OpenTelemetryActivityInboundInterceptor(ctx) })],
    },
  });
  try {
    await worker.run();
  } finally {
    await otel.shutdown();
  }
}

main().then(
  () => void process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  }
);
