// @@@SNIPSTART typescript-hello-activity
import {ApplicationFailure} from "@temporalio/workflow";

export async function greet(name: string): Promise<string> {

  if(name !== undefined){
    throw ApplicationFailure.retryable("activity expected failure")
  }

  return `Hello, ${name}!`;
}
// @@@SNIPEND
