import {Client, Connection} from '@temporalio/client';
import {exampleFailedActivity} from './workflows';
import {nanoid} from 'nanoid';

async function run() {
    const connection = await Connection.connect({address: 'localhost:7233'});

    const client = new Client({
        connection,
    });


    for (let i = 0; i < 1; i++) {

        const handle = await client.workflow.start(exampleFailedActivity, {
            taskQueue: 'hello-world',
            args: ['Temporal'],
            workflowId: 'workflow-' + nanoid(),
        });
        console.log(`Started workflow ${handle.workflowId}`);

    }


}

run().catch((err) => {
    console.error(err);
    process.exit(1);
});
