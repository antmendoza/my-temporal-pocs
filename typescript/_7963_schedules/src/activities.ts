import { ApplicationFailure } from "@temporalio/activity";

export async function greet(name: string): Promise<string> {

  throw ApplicationFailure.nonRetryable("", "")
//  return `Hello, ${name}!`;
}
