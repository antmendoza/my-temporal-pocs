import { DefaultLogger, Worker, Runtime } from '@temporalio/worker';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { ConsoleSpanExporter } from '@opentelemetry/sdk-trace-base';
import { NodeSDK } from '@opentelemetry/sdk-node';
import {
  OpenTelemetryActivityInboundInterceptor,
  makeWorkflowExporter,
} from '@temporalio/interceptors-opentelemetry/lib/worker';

import { JaegerExporter } from '@opentelemetry/exporter-jaeger';

// @ts-ignore
import * as mongodb from 'mongodb';
import {createActivities} from "./activities";


const DB_NAME = 'mydb'
const URL = `mongodb://localhost:27017/${DB_NAME}`;

async function main() {


  const resource = new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'interceptors-sample-worker',
  });
  // Export spans to console for simplicity
  const exporter = new ConsoleSpanExporter();

  const jaegerExporter = new JaegerExporter();


  const otel = new NodeSDK({ traceExporter: jaegerExporter, resource });
  await otel.start();

  // Silence the Worker logs to better see the span output in this sample
  Runtime.install({ logger: new DefaultLogger('WARN') });




  console.log("creating db connection ..  ");

  // Connect to db
  await accessDB(URL, DB_NAME)
      .then(async db => {


        console.log("starting worker ..  ");
        const worker = await Worker.create({
          workflowsPath: require.resolve('./workflows'),
          activities: createActivities(db),
          taskQueue: 'interceptors-opentelemetry-example',
          sinks: {
            exporter: makeWorkflowExporter(jaegerExporter, resource),
          },
          // Registers opentelemetry interceptors for Workflow and Activity calls
          interceptors: {
            // example contains both workflow and interceptors
            workflowModules: [require.resolve('./workflows')],
            activityInbound: [(ctx) => new OpenTelemetryActivityInboundInterceptor(ctx)],
          },
        });
        try {
          await worker.run();
        } finally {
          await otel.shutdown();
        }

      })
      .catch((err: Error) => {
        throw err;
      });




}

main().then(
  () => void process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  }
);


export function accessDB(
    url: string,
    dbName: string,
    options: mongodb.MongoClientOptions = {}
): Promise<mongodb.Db> {
  return new Promise((resolve, reject) => {
    mongodb.MongoClient.connect(url, {
      serverSelectionTimeoutMS: 1000
    })
        .then((client: { db: (arg0: string) => any; }) => {
          resolve(client.db(dbName));
        })
        .catch((reason: any) => {
          reject(reason);
        });
  });
}
