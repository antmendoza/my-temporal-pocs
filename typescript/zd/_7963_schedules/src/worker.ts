import {DefaultLogger, Runtime, Worker} from '@temporalio/worker';
import * as activities from './activities';

async function worker1Init() {

    try {

        const worker = await Worker.create({
            workflowsPath: require.resolve('./workflows'),
            activities,
            taskQueue: '_7963_schedules',
        });


        console.log("starting worker again")
        await worker.run()

    } catch (err) {
        await worker1Init();
    }



}

async function main() {

    // Silence the Worker logs to better see the span output in this sample
    Runtime.install({logger: new DefaultLogger('WARN')});

    await worker1Init();
}

main().then(
    () => {
        return;
    },
    (err) => {
        console.error(err);
//        process.exit(1);
    }
);
