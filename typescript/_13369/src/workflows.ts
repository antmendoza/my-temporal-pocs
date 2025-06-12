
import {CancellationScope, proxyActivities, sleep} from '@temporalio/workflow';
// Only import the activity types
import type * as activities from './activities';

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '1 minute',
  heartbeatTimeout : '3 seconds',
 // cancellationType: "WAIT_CANCELLATION_COMPLETED"
});

/** A workflow that simply calls an activity */
export async function example(name: string): Promise<string> {
  await sleep(1000)
  const greet1 = greet(name);
  const s = await greet1;
  return s;
}

