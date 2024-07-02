import {Context} from "@temporalio/activity";


export function initActivityLogger() {
    const log = Context.current().log;

    console.log = (...args) => log.info(...args);
    console.info = (...args) => log.info(...args);
    console.warn = (...args) => log.warn(...args);
    console.error = (...args) => log.error(...args);
    console.debug = (...args) => log.debug(...args);
    console.trace = (...args) => log.debug(...args);
}

export async function secondFunction(a: string): Promise<string> {
    initActivityLogger();

    const activityInfo = Context.current().info;
    console.log(
        "activity_workflow_id  [" + activityInfo.workflowExecution.workflowId + "] ;  "
        +
        "activity_run_id  [" + activityInfo.workflowExecution.runId + "] ;  "
        + "activityType  [" + activityInfo.activityType + "] + input [" + a + "]")
    return await firstFunction(a);
}

export async function firstFunction(a: string): Promise<string> {
    initActivityLogger();

    const activityInfo = Context.current().info;
    console.log(
        "activity_workflow_id  [" + activityInfo.workflowExecution.workflowId + "] ;  "
        +
        "activity_run_id  [" + activityInfo.workflowExecution.runId + "] ;  "
        + "activityType  [" + activityInfo.activityType + "] + input [" + a + "]")
    return `Hello!`;
}

