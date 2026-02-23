import {DefaultLogger, Worker, Runtime} from '@temporalio/worker';
import {Resource} from '@opentelemetry/resources';
import {SemanticResourceAttributes} from '@opentelemetry/semantic-conventions';
import {ConsoleSpanExporter} from '@opentelemetry/sdk-trace-base';
import {NodeSDK} from '@opentelemetry/sdk-node';
import {
    OpenTelemetryActivityInboundInterceptor,
    makeWorkflowExporter,
} from '@temporalio/interceptors-opentelemetry/lib/worker';
import * as activities from './activities';

async function main() {

    // Silence the Worker logs to better see the span output in this sample
    Runtime.install({logger: new DefaultLogger('WARN')});

    const worker = await Worker.create({
        workflowsPath: require.resolve('./workflows'),
        activities,
        taskQueue: 'interceptors-opentelemetry-example',
        // Registers opentelemetry interceptors for Workflow and Activity calls
        interceptors: {
            // example contains both workflow and interceptors
            workflowModules: [require.resolve('./workflows')],
        },
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
