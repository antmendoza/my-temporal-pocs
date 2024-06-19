import {Context} from "@temporalio/activity";

export async function greet(a: string): Promise<string> {


    const log = Context.current().log;
    let activityInfo = Context.current().info;
    log.info("workflow id  " + activityInfo.workflowExecution.workflowId + " ;  "
        +"activityType  " + activityInfo.activityType)
    return `Hello!`;
}

