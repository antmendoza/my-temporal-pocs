import { proxyActivities, sleep } from '@temporalio/workflow';
import type * as activities from './activities';
import * as wf from '@temporalio/workflow';

const {
  activityB,
  // activityA,
  // activityThatMustRunAfterA,
} = proxyActivities<typeof activities>({
  startToCloseTimeout: '30 seconds',
});

export const workflowId = 'patching-workflows-v3';
// @@@SNIPSTART typescript-patching-3
// v3
import { deprecatePatch } from '@temporalio/workflow';
import {isBlockedQuery} from "./workflows";

export async function myWorkflow(): Promise<void> {
  wf.setHandler(isBlockedQuery, () => true);


  deprecatePatch('my-change-id');
  await activityB();
  await sleep('1 days');
}
// @@@SNIPEND
