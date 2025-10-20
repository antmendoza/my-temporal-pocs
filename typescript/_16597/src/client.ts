import { Connection, Client } from '@temporalio/client';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { ConsoleSpanExporter } from '@opentelemetry/sdk-trace-base';
import { NodeSDK } from '@opentelemetry/sdk-node';
import { OpenTelemetryWorkflowClientInterceptor } from '@temporalio/interceptors-opentelemetry';
import { example } from './workflows';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-proto';
import {Runtime} from "@temporalio/worker";

async function run() {
  const resource = new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'interceptors-sample-client',
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
  const exporter = new ConsoleSpanExporter();

  const otel = new NodeSDK({ traceExporter: exporter, resource });
  await otel.start();
  // Connect to localhost with default ConnectionOptions,
  // pass options to the Connection constructor to configure TLS and other settings.
  const connection = await Connection.connect();
  // Attach the OpenTelemetryClientCallsInterceptor to the client.
  const client = new Client({
    connection,
  });
  try {
    for (let i = 0; i < 200; i++) {

      const result = await client.workflow.execute(example, {
        taskQueue: 'interceptors-opentelemetry-example',
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
