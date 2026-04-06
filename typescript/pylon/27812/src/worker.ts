import { NativeConnection, Worker } from '@temporalio/worker';
import * as activities from './activities';
import { HeartbeatLoggingInterceptor } from './heartbeat-interceptor';
import { loadClientConnectConfig } from '@temporalio/envconfig';

async function main() {

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

  const worker = await Worker.create({
    connection,
    workflowsPath: require.resolve('./workflows'),
    activities,
    taskQueue: 'temporal-ts',
    interceptors: {
      activity: [() => ({ inbound: new HeartbeatLoggingInterceptor() })],
    },
    namespace: config.namespace,
    shutdownGraceTime: '20 seconds',
  });
  try {
    await worker.run();
  } catch (e) {
    console.log(e);
  }
}

main().then(
  () => void process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  }
);
