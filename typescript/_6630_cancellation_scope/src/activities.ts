// @@@SNIPSTART typescript-hello-activity
import * as fs from 'fs';

export async function greet(name: string): Promise<string> {
  return `Hello, ${name}!`;
}
// @@@SNIPEND
