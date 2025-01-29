import { proxyActivities, sleep, startChild, workflowInfo } from '@temporalio/workflow';

import type * as activities from './activities';

export async function parentWorkflow(...names: string[]): Promise<string> {
  const pathResults = [];
  for (let i = 1; i < 6; i++) {
    const handle = await startChild(childWorkflow, {
      workflowId: workflowInfo().workflowId + '/child-' + i,
      args: [i],
    });
    pathResults.push(handle.result());
  }

  //this will fail the parent workflow if any of the child workflows fail
  //await Promise.all(pathResults);
  for await (const pendingResult of pathResults) {
    console.log(pendingResult);
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
  await sleep(1000 + i * 100);
  const throwError = i % 3 === 0;
  return await greet(throwError);
}
