import { Context } from '@temporalio/activity';

export async function greet(name: string): Promise<string> {

  Context.current().metricMeter.createGauge('my_custom_metric').set(Context.current().info.attempt);

 if( Context.current().info.attempt < 3){
   throw new Error("Failed to greet")
 }

  return `Hello, ${name}!`;
}
