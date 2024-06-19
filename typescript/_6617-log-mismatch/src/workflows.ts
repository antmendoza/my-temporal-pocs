import {log, proxyActivities, workflowInfo} from '@temporalio/workflow';
import type * as activities from './activities';

const {greet} = proxyActivities<typeof activities>({
    startToCloseTimeout: '1 minute',
});

export async function example(name: string): Promise<string> {

    log.info("workflow id  " + workflowInfo().workflowId+ " ;  "
        +" before invoking activity  " )

    let s = await greet("arg");

    log.info("workflow id  " + workflowInfo().workflowId+ " ;  "
        +" after invoking activity  " )

    return s;
}




