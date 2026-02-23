import { proxyActivities, proxyLocalActivities } from '@temporalio/workflow';
import type * as activities from '../activities';

const { extractAuthTokenFromContext, throwErrorIfTokenIncludesInitial } = proxyActivities<typeof activities>({
  startToCloseTimeout: '5 minutes',
});

const { generateNewEncryptedToken } = proxyLocalActivities<typeof activities>({
  startToCloseTimeout: '5 seconds',
});

export async function sampleWorkflow(): Promise<void> {

  await throwErrorIfTokenIncludesInitial();

  await throwErrorIfTokenIncludesInitial();

}
