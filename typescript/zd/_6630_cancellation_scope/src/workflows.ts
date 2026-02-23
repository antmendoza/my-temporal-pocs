
import {CancellationScope, proxyActivities, sleep} from '@temporalio/workflow';
// Only import the activity types
import type * as activities from './activities';

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '1 minute',
});

/** A workflow that simply calls an activity */
export async function example(name: string): Promise<string> {


  const scope = new CancellationScope();
  const promise = scope.run(() => sleep(1));
  scope.cancel(); // <-- Cancel the timer created in scope
  await promise; // <-- Throws CancelledFailure

  //
  // const cancellationScope = await CancellationScope.cancellable(async () => {
  //   const promise = sleep(1); // <-- Will be cancelled because it is attached to this closure's scope
  //   CancellationScope.current().cancel();
  //   await promise; // <-- Promise must be awaited in order for `cancellable` to throw
  // });


  return await greet(name);
}

