
import {SimpleWithDate} from "./types/simple_with_timestamp";

export async function greet(val: SimpleWithDate, date: Date): Promise<SimpleWithDate> {

  console.log("simple with date " + val)

  console.log("simple with date " + date)

  return val;

}

