import {
    ActivateInput,
    ApplicationFailure,
    ConcludeActivationInput,
    DisposeInput,
    proxyLocalActivities,
    sleep,
    WorkflowExecuteInput,
    WorkflowInboundCallsInterceptor,
    WorkflowInterceptorsFactory,
    WorkflowInternalsInterceptor
} from '@temporalio/workflow';
import type * as activities from './activities';
import {Next} from "@temporalio/worker";
import {ConcludeActivationOutput} from "@temporalio/workflow/lib/interceptors";

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

class MyWorkflowInternalsInterceptor implements WorkflowInternalsInterceptor {

    activate?(input: ActivateInput, next: Next<this, 'activate'>): void {
        console.log("activate " + JSON.stringify(input))

        try {
            // @ts-ignore
            next(input)

        } catch (e) {
            console.error("activate error " + e)
        }
    }


    // @ts-ignore
    concludeActivation?(input: ConcludeActivationInput, next: Next<this, 'concludeActivation'>): ConcludeActivationOutput {
        console.log("concludeActivation " + JSON.stringify(input))

        try {
            // @ts-ignore
            return next(input)

        } catch (e) {
            console.error("concludeActivation error " + e)
        }
    }


    dispose?(input: DisposeInput, next: Next<this, 'dispose'>): void {
        console.log("dispose " + JSON.stringify(input))


        try {
            // @ts-ignore
            next(input)

        } catch (e) {
            console.error("dispose error " + e)
        }

    }


}

// Export the interceptors
export const interceptors: WorkflowInterceptorsFactory = () => ({
    inbound: [new WorkflowErrorInterceptor()],
    // internals: [new MyWorkflowInternalsInterceptor()]
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
