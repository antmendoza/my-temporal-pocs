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



    //      we convert this error to an Application error to fail the workflow,
    //      see the interceptor implementation
    //if(name != null){
    //    throw Error("my error");
    //}

    // but for NDE with local activities the error is not propagated
    //await greet(name);


    console.log("before sleep")
    await sleep(5000)

    return "hello";
}

// Export the interceptors
export const interceptors: WorkflowInterceptorsFactory = () => ({
    inbound: [new WorkflowErrorInterceptor()],
});


class WorkflowErrorInterceptor implements WorkflowInboundCallsInterceptor {
    async execute(
        input: WorkflowExecuteInput,
        next: Next<WorkflowInboundCallsInterceptor, 'execute'>
    ): Promise<unknown> {
        try {
            return await next(input);
        } catch (err) {
            throw ApplicationFailure.nonRetryable("error");
        }
    }


}
