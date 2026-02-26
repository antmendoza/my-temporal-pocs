
import { proxyActivities, sleep, upsertMemo} from '@temporalio/workflow';
import type * as activities from './activities';

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '1 minute',
});



export async function example(name: string): Promise<string> {

  upsertMemo({
    "key1": "value",
  });

  await sleep(5_000);


  return await greet(name);
}
