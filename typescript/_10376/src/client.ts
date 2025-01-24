import fs from 'fs/promises';

import { Connection, Client, makeGrpcRetryInterceptor, defaultGrpcRetryOptions } from '@temporalio/client';
import { example } from './workflows';
import { InterceptingCall } from '@grpc/grpc-js';

/**
 * Schedule a Workflow connecting with mTLS, configuration is provided via environment variables.
 * Note that serverNameOverride and serverRootCACertificate are optional.
 *
 * If using Temporal Cloud, omit the serverRootCACertificate so that Node.js defaults to using
 * Mozilla's publicly trusted list of CAs when verifying the server certificate.
 */
async function run({
  address,
  namespace,
  clientCertPath,
  clientKeyPath,
  serverNameOverride,
  serverRootCACertificatePath,
  taskQueue,
}: Env) {
  // Note that the serverRootCACertificate is NOT needed if connecting to Temporal Cloud because
  // the server certificate is issued by a publicly trusted CA.
  let serverRootCACertificate: Buffer | undefined = undefined;
  if (serverRootCACertificatePath) {
    serverRootCACertificate = await fs.readFile(serverRootCACertificatePath);
  }

  //add grpc interceptor
  const connection = await Connection.connect({
    address,
    tls: {
      serverNameOverride,
      serverRootCACertificate,
      clientCertPair: {
        crt: await fs.readFile(clientCertPath),
        key: await fs.readFile(clientKeyPath),
      },
    },
    channelArgs: {
      'grpc.default_authority': address,
      'grpc.keepalive_permit_without_calls': 1,
      'grpc.keepalive_time_ms': 15_000,
      'grpc.keepalive_timeout_ms': 15_000,
    },
    interceptors: [
      (opts, nextCall) => {
        opts.deadline = Date.now() + 15_000;
        return new InterceptingCall(nextCall(opts));
      },
      makeGrpcRetryInterceptor(defaultGrpcRetryOptions()),
    ],
  });

  const client = new Client({ connection, namespace });


  for (let i = 0; i <2000; i++) {



    await client.workflow.start(example, {
      taskQueue,
      workflowId: `my-business-id-${Date.now()}`,
      args: ['Temporal'],
      workflowRunTimeout: '30 seconds', //I need to test start only (I don't plan start a worker for these workflows)
    }).catch((err) => {
      console.error(err);
    });

    if( i %50 == 0){
      console.log(`Started ${i} workflows and sleeping for 100ms`)
      await new Promise((resolve) => setTimeout(resolve, 50));
    }


  }


}

run(getEnv()).then(
  () => process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  },
);

// Helpers for configuring the mTLS client and worker samples

function requiredEnv(name: string): string {
  const value = process.env[name];
  if (!value) {
    throw new ReferenceError(`${name} environment variable is not defined`);
  }
  return value;
}

export interface Env {
  address: string;
  namespace: string;
  clientCertPath: string;
  clientKeyPath: string;
  serverNameOverride?: string; // not needed if connecting to Temporal Cloud
  serverRootCACertificatePath?: string; // not needed if connecting to Temporal Cloud
  taskQueue: string;
}

export function getEnv(): Env {
  return {
    address: requiredEnv('TEMPORAL_ADDRESS'),
    namespace: requiredEnv('TEMPORAL_NAMESPACE'),
    clientCertPath: requiredEnv('TEMPORAL_CLIENT_CERT_PATH'),
    clientKeyPath: requiredEnv('TEMPORAL_CLIENT_KEY_PATH'),
    serverNameOverride: process.env.TEMPORAL_SERVER_NAME_OVERRIDE,
    serverRootCACertificatePath: process.env.TEMPORAL_SERVER_ROOT_CA_CERT_PATH,
    taskQueue: process.env.TEMPORAL_TASK_QUEUE || 'hello-world-mtls',
  };
}
