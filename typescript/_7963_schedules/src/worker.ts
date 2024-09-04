import {DefaultLogger, Runtime, Worker} from '@temporalio/worker';
import * as activities from './activities';

async function main() {

    // Silence the Worker logs to better see the span output in this sample
    Runtime.install({logger: new DefaultLogger('WARN')});

    const worker = await Worker.create({
        workflowsPath: require.resolve('./workflows'),
        activities,
        taskQueue: '_7963_schedules',
    });

    await worker.run();
}

main().then(
    () => void process.exit(0),
    (err) => {
        console.error(err);
        process.exit(1);
    }
);
