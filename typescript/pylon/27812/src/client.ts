import { Client } from '@temporalio/client';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { ConsoleSpanExporter } from '@opentelemetry/sdk-trace-base';
import { NodeSDK } from '@opentelemetry/sdk-node';
import { OpenTelemetryWorkflowClientInterceptor } from '@temporalio/interceptors-opentelemetry';
import { example } from './workflows';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-proto';
import { Runtime, NativeConnection } from '@temporalio/worker';
import { loadClientConnectConfig } from '@temporalio/envconfig';

async function run() {
  const resource = new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'interceptors-sample-client',
  });

  const otlpEndpoint = process.env.OTEL_EXPORTER_OTLP_ENDPOINT || 'http://0.0.0.0:4318';
  let otlpGrpcUrl: string;
  if (otlpEndpoint.startsWith('http://') || otlpEndpoint.startsWith('https://')) {
    try {
      const u = new URL(otlpEndpoint);
      const host = u.hostname;
      const port = u.port || (u.protocol === 'https:' ? '443' : '80');
      const grpcPort = port === '4318' ? '4317' : port;
      otlpGrpcUrl = `grpc://${host}:${grpcPort}`;
    } catch {
      otlpGrpcUrl = otlpEndpoint.replace('http://', 'grpc://').replace('https://', 'grpc://');
    }
  } else if (otlpEndpoint.startsWith('grpc://')) {
    otlpGrpcUrl = otlpEndpoint;
  } else {
    const grpcPort = otlpEndpoint.endsWith(':4318') ? otlpEndpoint.replace(':4318', ':4317') : otlpEndpoint;
    otlpGrpcUrl = `grpc://${grpcPort}`;
  }

  Runtime.install({
    telemetryOptions: {
      metrics: {
        otel: {
          url: otlpGrpcUrl
        }
      },
    },
  });

  // Export spans to console for simplicity
  const exporter = new ConsoleSpanExporter();

  const otel = new NodeSDK({ traceExporter: exporter, resource });
  await otel.start();
  // Load connection config from ENV and config file (align with worker)
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
  // Attach the OpenTelemetryClientCallsInterceptor to the client.
  const client = new Client({ connection, interceptors: { workflow: [new OpenTelemetryWorkflowClientInterceptor()] } });
  try {
    for (let i = 0; i < 200; i++) {

      const result = await client.workflow.execute(example, {
        taskQueue: 'temporal-ts',
        workflowId: 'otel-example-0',
        args: ['Temporal'],
      });
      console.log(result); // Hello, Temporal!

    }

  } finally {
    await otel.shutdown();
  }
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
