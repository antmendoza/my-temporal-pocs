import { Client, Connection } from '@temporalio/client';
import { example } from './workflows';
import { getEnv } from './helpers';

async function run() {
  const { address, namespace, serverNameOverride, serverRootCACertificate, clientCert, clientKey } = await getEnv();

  const connection = await Connection.connect({
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
    metadata: {
      'temporal-namespace': namespace,
    },
  });

  const client = new Client({ connection, namespace });

  const taskQueue = 'interceptors-opentelemetry-example';
  try {
    for (let i = 0; i < 200; i++) {
      const result = await client.workflow.start(example, {
        taskQueue,
        workflowId: 'otel-example-0' + i,
        args: ['Temporal'],
      });
      console.log(result); // Hello, Temporal!
    }
  } finally {
  }
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
