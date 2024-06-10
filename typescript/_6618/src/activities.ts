// @@@SNIPSTART typescript-hello-activity
export async function greet(callback: () => void): Promise<string> {

  callback();
  return `Hello!`;
}
// @@@SNIPEND
