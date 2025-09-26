import { proxyActivities, proxyLocalActivities } from '@temporalio/workflow';
import type * as activities from '../activities';

const { extractAuthTokenFromContext, throwError } = proxyActivities<typeof activities>({
  startToCloseTimeout: '5 minutes',
});

const { generateNewEncryptedToken } = proxyLocalActivities<typeof activities>({
  startToCloseTimeout: '5 seconds',
});

export async function sampleWorkflow(): Promise<void> {
  await throwError(false);

  await throwError(true).catch(e => {
    // log.error("Caught error: " + e);
  })

  await throwError(false);
  //  log.info( clientContext + " | " + afterUpdatingContextInWorkflow + " | " + withContextValue + " | " + await extractAuthTokenFromContext());
}
