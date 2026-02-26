import { NativeConnection, Worker } from '@temporalio/worker';
import * as activities from './activities';


export async function startWorker( connection: NativeConnection,
                                   workerIdentity:string="worker") {
  const worker = await Worker.create({
    connection,
    namespace: 'default',
    taskQueue: 'hello-world',
    identity: workerIdentity,
    workflowsPath: require.resolve('./workflows'),
    activities,
  });
  return worker;
}


export async function run() {
  const connection = await NativeConnection.connect({
    address: 'localhost:7233',
  });


  try {
    const worker = await startWorker(connection);

    await worker.run();
  } finally {
    await connection.close();
  }
}
