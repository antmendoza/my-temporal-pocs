import {sleep} from "@temporalio/activity";
import { trace } from "@opentelemetry/api";


export const createActivities = () => ({

    async activity1(msg: string): Promise<string> {

        console.log("Run activity 1")

        const activeSpan = trace.getActiveSpan()
        if (activeSpan) {
            const traceId = activeSpan.spanContext().traceId;
            console.log(`Active Trace ID: ${traceId}`);
        } else {
            console.log("No active span found.");
        }

        await sleep(2000)
        return `${msg}`;
    },


    async activity2(msg: string): Promise<string> {
        console.log("Run activity 2")
        await sleep(3)
        return `${msg}`;
    },




});



