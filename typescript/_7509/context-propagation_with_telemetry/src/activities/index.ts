import { log } from '@temporalio/activity';
import { getContext } from '../context/context-injection';

export async function greet(name: string): Promise<string> {
  const propagatedContext = getContext();
  log.info(`Log from activity with customer ${propagatedContext.customer ?? 'unknown'}`);
  log.info(`Log from activity with customer ${JSON.stringify(propagatedContext)}`);
  return `Hello, ${name}!`;
}
