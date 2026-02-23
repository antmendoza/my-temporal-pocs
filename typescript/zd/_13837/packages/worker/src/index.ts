import { Worker, NativeConnection } from "@temporalio/worker";
import * as activities from "./activities";

async function run() {
  const connection = await NativeConnection.connect({
    address: "localhost:7233",
  });

  const worker = await Worker.create({
    connection,
    namespace: "default",
    taskQueue: "example-task-queue",
    workflowsPath: require.resolve("./workflows"),
    activities,
    interceptors: {
      workflowModules: [require.resolve("./interceptors")],
    },
  });

  console.log("Worker connected and ready to handle tasks");

  await worker.run();
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
