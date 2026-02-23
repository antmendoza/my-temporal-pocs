import { CancelledFailure, Context } from '@temporalio/activity';

export async function greet(name: string): Promise<string> {

  async function my_logic() {
    for (let i = 0; i < 10; i++) {
      // Simulate some work
      console.log('Activity is running > ' + i);
      await new Promise((f) => setTimeout(f, 1000));
      Context.current().heartbeat('');
    }
  }

  return await Promise.race([Context.current().cancelled, my_logic()])
    .then(() => `Hello, ${name}!`)
    .catch((e) => {
      if (e instanceof CancelledFailure) {
        console.log('Activity was cancelled > ' + e);
        // Cleanup
      }
      throw e;
    });

}
