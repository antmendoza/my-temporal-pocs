import {proxyLocalActivities, sleep} from '@temporalio/workflow';
import type * as activities from './activities';

const {greet} = proxyLocalActivities<typeof activities>({
    startToCloseTimeout: '1 minute',
});

// A workflow that simply calls an activity
export async function example(name: string): Promise<string> {

    await greet(name);

    console.log("before sleep")

    await sleep(5000)

    return "hello";
}
