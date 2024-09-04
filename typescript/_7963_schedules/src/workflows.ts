import {
    ApplicationFailure,
    proxyLocalActivities,
    SignalInput,
    sleep,
    WorkflowExecuteInput,
    WorkflowInboundCallsInterceptor,
    WorkflowInterceptorsFactory
} from '@temporalio/workflow';
import type * as activities from './activities';
import {Next} from "@temporalio/worker";

const {greet} = proxyLocalActivities<typeof activities>({
    startToCloseTimeout: '1 minute',
});

// A workflow that simply calls an activity
export async function example(name: string): Promise<string> {


    await greet(name);

    await sleep(5000)

    return "hello";
}

