import { proxyActivities, sleep } from '@temporalio/workflow';
import type * as activities from './activities';
import * as wf from "@temporalio/workflow";
import {isBlockedQuery} from "./workflows";

const {
  activityB,
  // activityA,
  // activityThatMustRunAfterA,
} = proxyActivities<typeof activities>({
  startToCloseTimeout: '30 seconds',
});

export const workflowId = 'patching-workflows-vFinal';
// @@@SNIPSTART typescript-patching-final
// vFinal
export async function myWorkflow(): Promise<void> {
  wf.setHandler(isBlockedQuery, () => true);


  await activityB();
  await sleep('1 days');
}
// @@@SNIPEND
