import { log } from '@temporalio/activity';
import { getContext } from '../context/context-injection';

export async function greet(name: string): Promise<string> {
  const propagatedContext = getContext();
  log.info(`1 ----------`);
  log.info(`${JSON.stringify(propagatedContext)}`);
  log.info(`2 ----------`);
  return `Hello, ${name}!`;
}
