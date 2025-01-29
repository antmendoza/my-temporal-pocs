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

  //  try{
  await Promise.all(pathResults);
  //  }catch (e) {
  //    console.log(e);
  //  }

  return 'done';
}

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '5 seconds',
});

export async function childWorkflow(i: number): Promise<string> {
  await sleep(1000 + (i * 100));
  return await greet(i % 3 === 0);
}
