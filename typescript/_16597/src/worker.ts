import { NativeConnection, Worker } from '@temporalio/worker';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import * as activities from './activities';
import { getEnv } from './helpers';

async function main() {
  const { address, namespace, serverNameOverride, serverRootCACertificate, clientCert, clientKey } = await getEnv();

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


  const worker = await Worker.create({
    workflowsPath: require.resolve('./workflows'),
    activities,
    taskQueue: 'interceptors-opentelemetry-example',
    maxConcurrentActivityTaskExecutions: 2,
    maxConcurrentWorkflowTaskExecutions: 10,
    namespace,
    connection,
  });
  try {
    await worker.run();
  } finally {
  }
}

main().then(
  () => void process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  }
);
