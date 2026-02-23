import { activityInfo } from '@temporalio/activity';

export async function greet(name: string): Promise<string> {
  return `Hello, ${name}!`;
}
