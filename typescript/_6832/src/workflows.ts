import { proxyActivities, defineSignal, sleep, Trigger, setHandler } from '@temporalio/workflow';

import type * as activities from './activities';

const { sendSlackNotification } = proxyActivities<typeof activities>({
  startToCloseTimeout: '1 minute',
});

export const completeSignal = defineSignal('completeConnection');

export async function waitForConnectionCompletion(id: string): Promise<boolean> {
  const completeTrigger = new Trigger<boolean>();
  setHandler(completeSignal, () => completeTrigger.resolve(true));
  const connectionCompleted = await Promise.race([completeTrigger, sleep('10 minutes')]);
  if (connectionCompleted) {
    return true;
  } else {
    await sendSlackNotification(id);
    return false;
  }
}