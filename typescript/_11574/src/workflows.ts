import { condition, proxyActivities, sleep } from '@temporalio/workflow';
import type * as activities from './activities';
import * as wf from '@temporalio/workflow';


const {
  makeHTTPRequest,
  completeSomethingAsync,
  // cancellableFetch  // todo: demo usage
} = proxyActivities<typeof activities>({
  retry: {
    initialInterval: '50 milliseconds',
    maximumAttempts: 2,
  },
  startToCloseTimeout: '30 seconds',
  scheduleToCloseTimeout: '1 minute',
});


export const unblockSignal = wf.defineSignal('unblock');

export async function httpWorkflow(): Promise<string> {
  let unblock = false;
  wf.setHandler(unblockSignal, () => void (unblock = true));
  const answer = await makeHTTPRequest();
  const a = await condition(()=> unblock, 2000)
  return ""+ a;
}

export async function asyncActivityWorkflow(): Promise<string> {
  const answer = await completeSomethingAsync();
  return `The Peon says: ${answer}`;
}
