import { ApplicationFailure } from '@temporalio/activity';

export async function greet(fail:boolean): Promise<string> {
  
  if(fail){
    throw ApplicationFailure.nonRetryable("Application failure");
  }
  return `done`;
}
