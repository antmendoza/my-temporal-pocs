import { ApplicationFailure } from '@temporalio/activity';

export async function greet(failWithNonRetryableError: boolean): Promise<string> {
  if (failWithNonRetryableError) {
    throw ApplicationFailure.nonRetryable('Application failure');
  }
  return `done`;
}
