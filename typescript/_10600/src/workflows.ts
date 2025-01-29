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
  await Promise.all(pathResults);

  return 'done';
}

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '5 seconds',
});

export async function childWorkflow(i: number): Promise<string> {
  await sleep(1000 + i * 100);
  const failWithNonRetryableError = i % 3 === 0;
  return await greet(failWithNonRetryableError);
}
