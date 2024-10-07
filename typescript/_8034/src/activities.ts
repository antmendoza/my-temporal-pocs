// @@@SNIPSTART typescript-hello-activity
import {ApplicationFailure} from "@temporalio/workflow";

const opentelemetry = require('@opentelemetry/api');
const {MeterProvider} = require('@opentelemetry/sdk-metrics');

export async function greet(name: string): Promise<string> {

    opentelemetry.metrics.setGlobalMeterProvider(new MeterProvider());


    if (name !== undefined) {

        // To record a metric event, we used the global singleton meter to create an instrument.
        const counter = opentelemetry.metrics.getMeter('default').createCounter('my_custom_metric');

        // record a metric event.
        counter.add(1, {attributeKey: 'attribute-value'});
        throw ApplicationFailure.retryable("activity expected failure")
    }

    return `Hello, ${name}!`;
}

// @@@SNIPEND
