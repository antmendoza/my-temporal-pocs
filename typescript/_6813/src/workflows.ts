import {proxyActivities, defineSignal, sleep, Trigger, setHandler, condition} from '@temporalio/workflow';

import {createActivities} from "./activities";

const {  myActivity} = proxyActivities<ReturnType<typeof createActivities>>({
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
    await myActivity(id);
    return false;
  }
}