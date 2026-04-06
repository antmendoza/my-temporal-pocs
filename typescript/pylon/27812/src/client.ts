import { Client } from '@temporalio/client';
import { OpenTelemetryWorkflowClientInterceptor } from '@temporalio/interceptors-opentelemetry';
import { example } from './workflows';
import { NativeConnection } from '@temporalio/worker';
import { loadClientConnectConfig } from '@temporalio/envconfig';
import { WorkflowIdConflictPolicy } from '@temporalio/common/src/workflow-options';

async function run() {
  // Load connection config from ENV and config file
  const configFile = process.env.TEMPORAL_CONFIG_FILE;

  const envProfile = process.env.TEMPORAL_PROFILE || 'default';

  console.log(`Loading '${envProfile}' profile from ${configFile}.`);

  const config = loadClientConnectConfig({
    // @ts-ignore
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
        workflowIdConflictPolicy: 'TERMINATE_EXISTING',
        args: ['Temporal'],
      });
      console.log(result); // Hello, Temporal!
    }
  } catch (e) {
    console.log(e);
  }
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
