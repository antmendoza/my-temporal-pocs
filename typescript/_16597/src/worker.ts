import { DefaultLogger, Runtime, Worker } from '@temporalio/worker';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { MetricReader } from '@opentelemetry/sdk-metrics';

import { NodeSDK } from '@opentelemetry/sdk-node';
import {
  makeWorkflowExporter,
  OpenTelemetryActivityInboundInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/worker';
import * as activities from './activities';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-proto';
import { PrometheusExporter } from '@opentelemetry/exporter-prometheus';

async function main() {
  const resource = new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'interceptors-sample-worker',
  });

  Runtime.install({
    logger: new DefaultLogger('WARN'),

    telemetryOptions: {
      metrics: {
        prometheus: {
          //     // Depending on you execution environment, you might need to set the host to `0.0.0.0` instead;
          //     // beware however that doing so in environments where this is not needed might expose your
          //     // metrics to the public Internet. This is why we default to the safer value of `127.0.0.1`.
          bindAddress: '127.0.0.1:9091',
        },
      },
    },
  });

  // Export spans to console for simplicity
  const exporter = new OTLPTraceExporter();

  const metricReader: MetricReader = new PrometheusExporter({
    //   // Depending on you execution environment, you might need to set `host` to `0.0.0.0` instead;
    //   // beware however that doing so in environments where this is not needed might expose your metrics
    //   // to the public Internet. This is why we default to the safer value of `127.0.0.1`.
    host: '127.0.0.1',

    //   // Runtime's metrics will be exposed on port 9091, Node's metrics on 9092.
    port: 9092,
  });

  const otel = new NodeSDK({
    traceExporter: exporter,
    // @ts-ignore
    metricReader,
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
