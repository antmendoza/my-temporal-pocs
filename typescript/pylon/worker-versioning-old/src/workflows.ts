import { proxyActivities, sleep } from '@temporalio/workflow';
import type * as activities from './activities';

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '10 seconds',
});

export async function greetingWorkflow(name: string): Promise<string> {
  const first = await greet(name);
  await sleep('20 seconds');
  await sleep('2 seconds');
  await sleep('2 seconds');
  await sleep('2 seconds');
  const second = await greet(`${name} again`);
  return `${first} | ${second}`;
}
