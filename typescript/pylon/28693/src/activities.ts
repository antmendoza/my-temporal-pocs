import { Context } from '@temporalio/activity';

export async function greet(name: string): Promise<string> {

  Context.current().metricMeter.createGauge('my_custom_metric_gauge').set(Context.current().info.attempt);
  Context.current().metricMeter.createCounter('my_custom_metric_counter').add(1);

 if( Context.current().info.attempt < 3){
   throw new Error("Failed to greet")
 }

  return `Hello, ${name}!`;
}
