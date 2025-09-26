import { ApplicationFailure, log } from '@temporalio/activity';
import { getContext } from '../context/context-injection';

export async function extractCustomerNameFromContext(): Promise<string> {
  const propagatedContext = getContext();
  log.info(`Log from activity with customer ${propagatedContext.customer ?? 'unknown'}`);
  return `Hello, ${propagatedContext.customer}!`;
}





export async function localToModifyCustomerName(): Promise<string> {

  getContext().customer = "localToModifyCustomerName";

  const propagatedContext = getContext();

  log.info(`Log from activity with customer ${propagatedContext.customer ?? 'unknown'}`);

  return `Hello, ${propagatedContext.customer}!`;
}




export async function throwError(): Promise<string> {
  throw ApplicationFailure.nonRetryable("This is a non-retryable error from activity");
}
