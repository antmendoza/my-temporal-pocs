import { NativeConnection, Runtime, Worker } from '@temporalio/worker';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { NodeSDK } from '@opentelemetry/sdk-node';
import {
  makeWorkflowExporter,
  OpenTelemetryActivityInboundInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/worker';
import * as activities from './activities';
import { HeartbeatLoggingInterceptor } from './heartbeat-interceptor';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-proto';
import { loadClientConnectConfig } from '@temporalio/envconfig';

async function main() {
  const resource = new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'antmendoza_worker',
  });

  const otlpEndpoint = process.env.OTEL_EXPORTER_OTLP_ENDPOINT || 'http://0.0.0.0:4318';
  // Build a gRPC URL for metrics from the provided (likely HTTP/OTLP) endpoint.
  const otlpGrpcUrl = process.env.OTEL_EXPORTER_GRPC_ENDPOINT || 'grpc://0.0.0.0:4317';

  Runtime.install({
    telemetryOptions: {
      metrics: {
        otel: {
          url: otlpGrpcUrl,
        },
      },
    },
  });

  // Export spans to console for simplicity
  const exporter = new OTLPTraceExporter({
    url: otlpEndpoint,
  });

  const otel = new NodeSDK({
    traceExporter: exporter,
    resource,
  });

  await otel.start();

  // Load connection config from ENV and config file
  const configFile = process.env.TEMPORAL_CONFIG_FILE || './config/config.json';

  const envProfile = process.env.TEMPORAL_PROFILE || 'default';

  console.log(`Loading '${envProfile}' profile from ${configFile}.`);

  const config = loadClientConnectConfig({
    configSource: { path: configFile },
  });

  console.log(`Loaded '${envProfile}' profile from ${configFile}.`);

  console.log(`  Address: ${config.connectionOptions.address}`);
  console.log(`  Namespace: ${config.namespace}`);
  console.log(`  gRPC Metadata: ${JSON.stringify(config.connectionOptions.metadata)}`);

  const connection = await NativeConnection.connect(config.connectionOptions);

  const worker = await Worker.create({
    connection,
    workflowsPath: require.resolve('./workflows'),
    activities,
    taskQueue: 'temporal-ts',
    sinks: {
      exporter: makeWorkflowExporter(exporter, resource),
    },
    interceptors: {
      activity: [() => ({ inbound: new HeartbeatLoggingInterceptor() })],
    },
    namespace: config.namespace,
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
