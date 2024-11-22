import {DefaultLogger, Runtime, Worker} from '@temporalio/worker';
import * as activities from './activities';
import {getDataConverter} from "./data-converter";

async function main() {

    // Silence the Worker logs to better see the span output in this sample
    Runtime.install({logger: new DefaultLogger('WARN')});

    const worker = await Worker.create({
        workflowsPath: require.resolve('./workflows'),
        activities,
        taskQueue: 'interceptors-opentelemetry-example',
        dataConverter: await getDataConverter(),
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
