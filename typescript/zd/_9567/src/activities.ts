import {ApplicationFailure} from "@temporalio/workflow";

export async function greet(name: string): Promise<string> {


    try {
        throw new Error("Hello World");
    } catch (error: any) {

        throw ApplicationFailure.fromError(error,
            {
                nonRetryable: true,
                message: "my custom message",
                cause: error, // error.cause
                details: [buildStackTrace(error)]
            });
    }

    return `Hello, ${name}!`;
}



function buildStackTrace(error: any) {
    const trace = [];
    let currentError = error;
    while (currentError) {
        trace.push(`Message: ${currentError.message}`);
        trace.push(`Stack:\n${currentError.stack}`);
        currentError = currentError.cause;
        // Traverse the cause chain
        if (currentError) {
            trace.push(`\nCaused by:\n`);
        }
    }
    return trace.join('\n');
}