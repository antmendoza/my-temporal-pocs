import { proxyActivities } from '@temporalio/workflow';

// @@@SNIPSTART typescript-activity-deps-workflow
import type { createActivities } from './activities';

// Note usage of ReturnType<> generic since createActivities is a factory function
const { client } = proxyActivities<ReturnType<typeof createActivities>>({
  startToCloseTimeout: '30 seconds',
});
// @@@SNIPEND

export async function dependencyWF(): Promise<string> {
  const result = await client('Hello');
  return `${result}`;
}
