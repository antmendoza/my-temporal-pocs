import { DefaultLogger, NativeConnection, Runtime, Worker } from '@temporalio/worker';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { MetricReader } from '@opentelemetry/sdk-metrics';
import { NodeSDK } from '@opentelemetry/sdk-node';
import {
  makeWorkflowExporter,
} from '@temporalio/interceptors-opentelemetry/lib/worker';
import * as activities from './activities';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-proto';
import { PrometheusExporter } from '@opentelemetry/exporter-prometheus';
import { getEnv } from './helpers';

async function main() {


  Runtime.install({
    logger: new DefaultLogger('WARN'),

    telemetryOptions: {
      metrics: {
        prometheus: {
          bindAddress: '127.0.0.1:9091',
        },
      },
    },
  });

  const { address, namespace, serverNameOverride, serverRootCACertificate, clientCert, clientKey } =
    await getEnv();

  const connection = await NativeConnection.connect({
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
  });



  const resource = new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'interceptors-sample-worker',
  });


  // Export spans to console for simplicity
  const exporter = new OTLPTraceExporter();

  const metricReader: MetricReader = new PrometheusExporter({
    host: '127.0.0.1',
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
    maxConcurrentActivityTaskExecutions:2,
    maxConcurrentWorkflowTaskExecutions: 4,
    // Registers opentelemetry interceptors for Workflow and Activity calls
    interceptors: {
      // example contains both workflow and interceptors
      workflowModules: [require.resolve('./workflows')],
    },
    namespace,
    connection
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
