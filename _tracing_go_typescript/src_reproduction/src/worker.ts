import {DefaultLogger, makeTelemetryFilterString, Runtime, Worker} from '@temporalio/worker';
import {Resource} from '@opentelemetry/resources';
import {SEMRESATTRS_SERVICE_NAME} from '@opentelemetry/semantic-conventions';
import {NodeSDK} from '@opentelemetry/sdk-node';
import {
    makeWorkflowExporter,
    OpenTelemetryActivityInboundInterceptor, OpenTelemetryActivityOutboundInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/worker';

import {JaegerExporter} from '@opentelemetry/exporter-jaeger';


import {createActivities} from "./activities";
import {propagation} from "@opentelemetry/api";
import {CompositePropagator, W3CBaggagePropagator, W3CTraceContextPropagator} from "@opentelemetry/core";
import {JaegerPropagator} from "@opentelemetry/propagator-jaeger";
import {otelSdk, resource, traceExporter} from "./instrumentation";


function initializeRuntime() {
    Runtime.install({
        // Configure a logger that will collect all log messages emitted by the Worker,
        // including those emitted through the Workflow's and Activity's context logger APIs.
        // See the 'custom-logger' sample for an example of how to build a logger that
        // processes log messages using a third party logging library.
        //
        // IMPORTANT: Make sure to configure the `telemetryOptions.logging` property
        //            below to also collect logs emitted by the native runtime.
        //
        logger: new DefaultLogger('WARN'),

        telemetryOptions: {
            // Configure the OpenTelemetry metrics exporter for the native runtime.
            //
            // IMPORTANT: Uncomment either of the two options below to choose the desired exporter,
            //            as appropriate for your environment.
            //

            // (1) A metric exporter that periodically sends metrics to a server using the
            //     _OTLP over gRPC_ protocol. This is the most common configuration when connecting
            //     to a metrics collector. Note that the _OTLP over HTTP_ protocol (i.e. port 4318)
            //     is not supported for Runtime's metrics.
            //
            // metrics: {
            //   otel: { url: 'http://127.0.0.1:4317', metricsExportInterval: '1s' },
            // },

            // (2) A metrics exporter that exposes metrics as an HTTP endpoint that can be queried
            //     by your collector. Just point a browser on http://127.0.0.1:9091/metrics to
            //     visualize your Worker's metrics.
            //
            // metrics: {
            //   prometheus: {
            //     // Depending on you execution environment, you might need to set the host to `0.0.0.0` instead;
            //     // beware however that doing so in environments where this is not needed might expose your
            //     // metrics to the public Internet. This is why we default to the safer value of `127.0.0.1`.
            //     bindAddress: '127.0.0.1:9091',
            //   },
            // },

            // Configure forwarding of log entries emitted by the native runtime through the TypeScript
            // logger (i.e. the one configured using the `logger` property above). This is required for
            // example if you would like to forward all worker logs to a log aggregation service.
            //
            logging: {
                forward: {},
                filter: makeTelemetryFilterString({ core: 'INFO', other: 'INFO' }),
            },
        },
    });
}

async function main() {

    initializeRuntime();
    /**
    const compositePropagator = new CompositePropagator({
        propagators: [
            new W3CTraceContextPropagator(),
            new W3CBaggagePropagator(),
            new JaegerPropagator(),
        ],
    });
     */


    console.log("starting worker ..  ");
    const worker = await Worker.create({
        workflowsPath: require.resolve('./workflows'),
        activities: createActivities(),
        taskQueue: 'ts-taskqueue',
        sinks:traceExporter && {
            exporter: makeWorkflowExporter(traceExporter, resource),
        },
        // Registers opentelemetry interceptors for Workflow and Activity calls
        interceptors: traceExporter &&  {
            // example contains both workflow and interceptors
            workflowModules: [require.resolve('./workflows')],
            activity: [
                (ctx) => ({
                    inbound: new OpenTelemetryActivityInboundInterceptor(ctx),
                    outbound: new OpenTelemetryActivityOutboundInterceptor(ctx),
                }),
            ],
        },
    });
    try {
        await worker.run();
    } finally {
        await otelSdk.shutdown();
    }

}

main().then(
    () => void process.exit(0),
    (err) => {
        console.error(err);
        process.exit(1);
    }
);

