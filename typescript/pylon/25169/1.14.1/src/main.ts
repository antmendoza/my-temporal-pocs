import { startWorker } from './worker';
import { NativeConnection } from '@temporalio/worker';
import { startWorkflow } from './client';
import { loadClientConnectConfig } from '@temporalio/envconfig';
import { Client, Connection } from '@temporalio/client';
import { WorkerStatus } from '@temporalio/worker/src/worker';

async function self_start_worker( workerIdentity:string) {
  const connection = await NativeConnection.connect({
    address: 'localhost:7233',
  });

  const worker = await startWorker(connection, workerIdentity);

  void worker.run();

  return worker;
}

async function getClient() {
  const config = loadClientConnectConfig();
  const connection = await Connection.connect(config.connectionOptions);
  const client = new Client({ connection });
  return client;
}

async function run() {
  const worker = await self_start_worker("first-worker");

  const workflowHandle = await startWorkflow(await getClient());


  //wait for the worker to process the first workflow task and schedule the timer
  await new Promise((resolve) => setTimeout(resolve, 100));

  worker.shutdown();

  while (worker.getStatus().runState != "STOPPED") {
    await new Promise((resolve) => setTimeout(resolve, 100));
  }


  await workflowHandle.signal('hello');
  await self_start_worker("second-worker");


  await new Promise((resolve) => setTimeout(resolve, 10_000));

}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
// @@@SNIPEND
