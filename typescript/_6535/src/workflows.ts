// @@@SNIPSTART typescript-hello-workflow
import {CancellationScope, proxyActivities, sleep} from '@temporalio/workflow';
// Only import the activity types
import type * as activities from './activities';

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '1 minute',
  heartbeatTimeout : '3 seconds'
});

/** A workflow that simply calls an activity */
export async function example(name: string): Promise<string> {
  await sleep(1000)
  const ms = 10_000;
  await sleep(ms)
  let greet1 = greet(name);
  let s = await greet1;
  return s;
}
// @@@SNIPEND
