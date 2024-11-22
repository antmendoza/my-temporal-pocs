import {Client, Connection} from '@temporalio/client';
import {example} from './workflows';

async function run() {


    // Connect to localhost with default ConnectionOptions,
    // pass options to the Connection constructor to configure TLS and other settings.
    const connection = await Connection.connect();
    // Attach the OpenTelemetryClientCallsInterceptor to the client.
    const client = new Client({
        connection,
    });
    try {
        const result = await client.workflow.execute(example, {
            taskQueue: 'interceptors-opentelemetry-example',
            workflowId: 'my-wf' + Math.random(),
            args: ['Temporal'],
        });
        console.log(result); // Hello, Temporal!
    }finally {

    }
}

run().catch((err) => {
    console.error(err);
    process.exit(1);
});
