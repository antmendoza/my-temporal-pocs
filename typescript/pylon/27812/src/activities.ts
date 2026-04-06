import { Context } from '@temporalio/activity';


export async function greet(name: string): Promise<string> {


  for (let i = 0; i < 100; i++) {
    await Context.current().heartbeat("");
    await Context.current().sleep(200).catch((e) => {
      console.log(">>>> " + e);
    })

  }


  return `Hello, ${name}!`;
}
