import {Client, Connection} from '@temporalio/client';
import {OpenTelemetryWorkflowClientInterceptor} from '@temporalio/interceptors-opentelemetry';
import {example} from './workflows';

async function run() {

  const connection = await Connection.connect();

    const client = new Client({
        connection,
    });

    const result = await client.workflow.execute(example, {
        taskQueue: '_7963_schedules',
        workflowId: 'my-wf' + Math.random(),
        args: ['Temporal'],
    });

    console.log(result); // Hello, Temporal!

}

run().catch((err) => {
    console.error(err);
    process.exit(1);
});
