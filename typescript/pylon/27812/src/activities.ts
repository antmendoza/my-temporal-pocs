import { Context } from '@temporalio/activity';

export async function greet(name: string): Promise<string> {
  for (let i = 0; i < 100; i++) {
    Context.current().heartbeat('');
    await Context.current()
      .sleep(200)
      .catch((e) => {
        // https://typescript.temporal.io/api/namespaces/activity#cancellation
        // console.log('>>>> ' + e);
      throw e;
      });

  }

  return `Hello, ${name}!`;
}
