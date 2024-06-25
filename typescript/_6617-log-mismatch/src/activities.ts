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

export async function greet(a: string): Promise<string> {
    initActivityLogger();

    const log = Context.current().log;
    const activityInfo = Context.current().info;
    console.log("activity_workflow_id  [" + activityInfo.workflowExecution.workflowId + "] ;  "
        +"activityType  [" + activityInfo.activityType+"]")
    return `Hello!`;
}

