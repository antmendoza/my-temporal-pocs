import {
  DefaultLogger,
  LogLevel,
  makeTelemetryFilterString,
  NativeConnection,
  Runtime,
  Worker
} from '@temporalio/worker';
import winston from 'winston';
import * as activities from './activities';


function getLogger(param: { ddSource: string; prettyPrint: boolean; logLevel: string; service: string; env: string }) {
  return winston.createLogger({
    level: param.logLevel,
    format: winston.format.json(),
    defaultMeta: { service: param.service },

    transports: [
      new winston.transports.Console(),
      new winston.transports.File({ filename: 'combined_.log' }),
    ]

  });

}

const logger = getLogger({
  logLevel: "info",
  env: "PROD",
  prettyPrint: false,
  ddSource: 'nodejs',
  service: 'temporal',
});

const winstonLogger = new DefaultLogger("INFO" as LogLevel, (entry) => {
  let additionalFields = {};
  // This is a hack we put in to let us process errors
  if (entry?.meta?.error && entry.meta.error instanceof Error) {
    additionalFields = {
      message: `${entry.message}: ${entry.meta.error.message}`,
      stack: entry.meta.error.stack,
    };
  }
  logger.log({
    level: entry.level.toLowerCase(),
    message: entry.message,
    timestamp: new Date(Number(entry.timestampNanos / 1_000_000n)).toISOString(),
    ...additionalFields,
    ...entry.meta,
  });
});

async function run() {


  Runtime.install({
    logger: winstonLogger,
    telemetryOptions: {
      logging: {
        filter: makeTelemetryFilterString({
          core: "INFO" as LogLevel,
          other: "INFO" as LogLevel,
        }),
        forward: {},
      },
    },
  });



  const connection = await NativeConnection.connect({
    address: 'localhost:7233',
  });
  const worker = await Worker.create({
    connection,
    namespace: 'default',
    taskQueue: 'hello-world',
    // Workflows are registered using a path as they run in a separate JS context.
    workflowsPath: require.resolve('./workflows'),
    activities,
  });


  await worker.run();
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});



