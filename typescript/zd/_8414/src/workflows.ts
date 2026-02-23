import {ApplicationFailure, executeChild, proxyActivities, workflowInfo} from '@temporalio/workflow';
import type * as activities from './activities';

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '1 minute',
  retry: {
    maximumAttempts: 2,
  },
});

export async function exampleFailedActivity(name: string): Promise<string> {
  return await greet(name);
}

export async function exampleParentChild(name: string): Promise<string> {

  await executeChild(exampleFailedChild,{
    workflowId: workflowInfo().workflowId + '/child',
    args: [name],
  })

  return await greet(name);
}


export async function exampleFailedChild(name: string): Promise<string> {

  if(name !== undefined){
    //throw ApplicationFailure.retryable("activity expected failure")
  }


  return await greet(name);
}
// @@@SNIPEND
