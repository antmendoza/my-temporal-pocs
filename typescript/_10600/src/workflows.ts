import { proxyActivities, sleep, startChild, workflowInfo } from '@temporalio/workflow';

import type * as activities from './activities';

const addSleep = true;

const waitWithPromiseAll = false;

export async function parentWorkflow(...names: string[]): Promise<string> {
  const childs = [];

  for (let i = 1; i < 3; i++) {
    const workflowId = workflowInfo().workflowId + '/child-' + i;
    const handle = await startChild(childWorkflow, {
      workflowId,
      args: [i],
    });
    childs.push(handle.result());
  }

  if (addSleep) {
    console.log('before sleep');
    await sleep(5_000);
  }

  if (waitWithPromiseAll) {
    await Promise.all(childs);
  } else {
    for await (const pendingResult of childs) {
      console.log(pendingResult);
    }
  }
  return 'done';
}

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '5 seconds',
  retry: {
    maximumAttempts: 1,
  },
});

export async function childWorkflow(i: number): Promise<string> {
  if (addSleep) {
    console.log('before sleep');
    await sleep(5_000);
  }
  return await greet(true);
}
