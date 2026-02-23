import { ApplicationFailure } from '@temporalio/activity';

export async function greet(throwError: boolean): Promise<string> {
  if (throwError) {
    throw Error('My error');
  }
  return `done`;
}
