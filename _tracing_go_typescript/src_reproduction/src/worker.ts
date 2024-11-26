import {DefaultLogger, Runtime, Worker} from '@temporalio/worker';
import {Resource} from '@opentelemetry/resources';
import {SEMRESATTRS_SERVICE_NAME} from '@opentelemetry/semantic-conventions';
import {ConsoleSpanExporter} from '@opentelemetry/sdk-trace-base';
import {api, node, NodeSDK} from '@opentelemetry/sdk-node';
import {
    makeWorkflowExporter,
    OpenTelemetryActivityInboundInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/worker';

import {JaegerExporter} from '@opentelemetry/exporter-jaeger';


import {createActivities} from "./activities";
import {propagation} from "@opentelemetry/api";
import {CompositePropagator, W3CBaggagePropagator, W3CTraceContextPropagator} from "@opentelemetry/core";
import {JaegerPropagator} from "@opentelemetry/propagator-jaeger";


async function main() {

    const compositePropagator = new CompositePropagator({
        propagators: [
            new W3CTraceContextPropagator(),
            new W3CBaggagePropagator(),
            new JaegerPropagator(),
        ],
    });

    const resource = new Resource({
        [SEMRESATTRS_SERVICE_NAME]: 'tracing',
    });

    const jaegerExporter = new JaegerExporter();






    const otel = new NodeSDK({
        traceExporter: jaegerExporter, resource,
    });

    propagation.setGlobalPropagator(
        compositePropagator,
    );



    await otel.start();

    // Silence the Worker logs to better see the span output in this sample
    Runtime.install({
        logger: new DefaultLogger('WARN')
    });


    console.log("starting worker ..  ");
    const worker = await Worker.create({
        workflowsPath: require.resolve('./workflows'),
        activities: createActivities(),
        taskQueue: 'ts-taskqueue',
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

}

main().then(
    () => void process.exit(0),
    (err) => {
        console.error(err);
        process.exit(1);
    }
);

