import { ApplicationFailure, log } from '@temporalio/activity';
import { getContext } from '../context/context-injection';
import { uuid4 } from '@temporalio/workflow';

export async function extractAuthTokenFromContext(): Promise<string> {
  const propagatedContext = getContext();
  log.info(`Log from activity with authToken ${propagatedContext.authToken ?? 'unknown'}`);
  return `Hello, ${propagatedContext.authToken}!`;
}

export async function generateNewEncryptedToken(): Promise<string> {
  const token = uuid4();
  log.info(`newEncryptedToken ${token}`);

  return 'new_token_' + token;
}

export async function throwErrorIfTokenIncludesInitial(): Promise<void> {

  if(getContext().authToken?.includes("initial")) {
    throw ApplicationFailure.nonRetryable('AuthError', 'AuthError');
  }

  return;
}
