import { ApplicationFailure, log } from '@temporalio/activity';
import { getContext } from '../context/context-injection';
import { uuid4 } from '@temporalio/workflow';

export async function extractCustomerNameFromContext(): Promise<string> {
  const propagatedContext = getContext();
  log.info(`Log from activity with customer ${propagatedContext.customer ?? 'unknown'}`);
  return `Hello, ${propagatedContext.customer}!`;
}

export async function newEncryptedToken(): Promise<string> {
  const token = uuid4();
  log.info(`newEncryptedToken ${token}`);

  return token;
}

export async function throwError(throw_: boolean): Promise<void> {
  if (!throw_) return;
  throw ApplicationFailure.nonRetryable('This is a non-retryable error from activity', 'AuthError');
}
