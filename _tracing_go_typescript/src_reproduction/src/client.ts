import { Connection, Client } from '@temporalio/client';
import { Resource } from '@opentelemetry/resources';
import {ATTR_SERVICE_NAME, SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { ConsoleSpanExporter } from '@opentelemetry/sdk-trace-base';
import { NodeSDK } from '@opentelemetry/sdk-node';
import { OpenTelemetryWorkflowClientInterceptor } from '@temporalio/interceptors-opentelemetry';
import {ts_workflow} from "./workflows";
import { propagation } from '@opentelemetry/api';
import {CompositePropagator, W3CBaggagePropagator, W3CTraceContextPropagator} from "@opentelemetry/core";
import { JaegerPropagator } from '@opentelemetry/propagator-jaeger';

async function run() {

  /**
  propagation.setGlobalPropagator(
      new CompositePropagator({
        propagators: [
          new W3CTraceContextPropagator(),
          new W3CBaggagePropagator(),
          new JaegerPropagator(),
        ],
      }),
  );
   */


  // Connect to localhost with default ConnectionOptions,
  // pass options to the Connection constructor to configure TLS and other settings.
  const connection = await Connection.connect();
  // Attach the OpenTelemetryClientCallsInterceptor to the client.
  const client = new Client({
    connection,
    interceptors: {
      workflow: [new OpenTelemetryWorkflowClientInterceptor()],
    },
  });
    const result = await client.workflow.execute(ts_workflow, {
      taskQueue: 'ts-taskqueue',
      workflowId: 'otel-example-0' + Math.random(),
      args: ['Temporal'],
    });
    console.log(result); // Hello, Temporal!
  
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
