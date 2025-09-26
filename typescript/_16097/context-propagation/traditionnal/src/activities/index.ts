import { log } from '@temporalio/activity';
import { getContext } from '../context/context-injection';

export async function extractCustomerNameFromContext(): Promise<string> {
  const propagatedContext = getContext();
  log.info(`Log from activity with customer ${propagatedContext.customer ?? 'unknown'}`);
  return `Hello, ${propagatedContext.customer}!`;
}
