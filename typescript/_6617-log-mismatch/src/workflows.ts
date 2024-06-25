import {log, proxyActivities, workflowInfo} from '@temporalio/workflow';
import type * as activities from './activities';

const {greet} = proxyActivities<typeof activities>({
    startToCloseTimeout: '1 minute',
});



export function initWorkflowLogger() {
    console.log = (...args) => log.info(...args);
    console.info = (...args) => log.info(...args);
    console.warn = (...args) => log.warn(...args);
    console.error = (...args) => log.error(...args);
    console.debug = (...args) => log.debug(...args);
    console.trace = (...args) => log.debug(...args);
}

export async function example(name: string): Promise<string> {

    initWorkflowLogger()
    console.log("workflow_id [" + workflowInfo().workflowId+ "] ;  "
        +" before invoking activity  " )

    const s = await greet("arg");

    console.log("workflow id [" + workflowInfo().workflowId+ "] ;  "
        +" after invoking activity  " )

    return s;
}




