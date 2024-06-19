
import { proxyActivities } from '@temporalio/workflow';
// Only import the activity types
import type * as activities from './activities';
import {SimpleWithDate} from "./types/simple_with_timestamp";

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '1 minute',
});

/** A workflow that simply calls an activity */
export async function example(name: string): Promise<string> {

  const now = new Date();
  const simple: SimpleWithDate=  {
    name:'',
    date: now,
  };

  const s = SimpleWithDate.fromJSON( await greet(simple, now));
  return "Hello [" + s.date+"]!";
}

