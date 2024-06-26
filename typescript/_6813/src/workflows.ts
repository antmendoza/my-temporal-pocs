import {proxyActivities, defineSignal, sleep, Trigger, setHandler, condition} from '@temporalio/workflow';

import {createActivities} from "./activities";

const {  myActivity, myAxiosActivity} = proxyActivities<ReturnType<typeof createActivities>>({
  startToCloseTimeout: '1 minute',
});


export async function waitForConnectionCompletion(id: string): Promise<string> {

  await myActivity("");

  return (await myAxiosActivity()).field1;

}