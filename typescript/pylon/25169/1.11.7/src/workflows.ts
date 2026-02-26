// @@@SNIPSTART typescript-hello-workflow
import { proxyActivities, sleep, upsertMemo} from '@temporalio/workflow';
// Only import the activity types
import type * as activities from './activities';

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '1 minute',
});





//initiate the workflow sith signal with start
export async function example(name: string): Promise<string> {

  upsertMemo({
    "key1": "value",
  });

  await sleep(5_000);

  //while the workflow is sleeping the client send a signal

  //after some time the timer fired.

  //NDE during workflow replay


  return await greet(name);
}
