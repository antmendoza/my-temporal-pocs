import { proxyActivities, sleep, startChild, workflowInfo } from '@temporalio/workflow';

import type * as activities from './activities';

export async function parentWorkflow(...names: string[]): Promise<string> {
  const pathResults = [];

  const childWorkflows = [];
  for (let i = 1; i < 3; i++) {
    const workflowId = workflowInfo().workflowId + '/child-' + i;
    const handle = await startChild(childWorkflow, {
      workflowId,
      args: [i],
    });

    childWorkflows.push(workflowId)
    pathResults.push(handle.result());
  }


  console.log('before sleep');

  await sleep(5000)


  console.log('childWorkflow', childWorkflows);

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
  //await sleep(1000 + i * 100);
  await sleep(5000);
  const throwError = i % 2 === 0;
  return await greet(true);
}
