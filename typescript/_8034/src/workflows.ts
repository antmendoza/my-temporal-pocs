import {proxyActivities} from '@temporalio/workflow';
import type * as activities from './activities';

const {greet} = proxyActivities<typeof activities>({
    startToCloseTimeout: '1 minute',
    retry: {
        maximumAttempts: 2,
    },
});

export async function exampleFailedActivity(name: string): Promise<string> {
    return await greet(name);
}



