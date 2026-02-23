export async function greet(name: string): Promise<string> {

  await new Promise((resolve) => setTimeout(resolve, 100));

  return `Hello, ${name}!`;
}
