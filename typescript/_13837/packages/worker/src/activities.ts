import { sleep } from "@temporalio/activity";

export async function greet(name: string): Promise<void> {
  console.log(`Hello, ${name}!`);
}

export async function sleepRandom(
    startMilliseconds: number,
    endMilliseconds: number,
    sequence: string
): Promise<void> {
  const duration =
      Math.random() * (endMilliseconds - startMilliseconds) + startMilliseconds;
  await sleep(duration);
}


export async function sleepRandomLocal(
    startMilliseconds: number,
    endMilliseconds: number,
    sequence: string
): Promise<void> {
  const duration =
      Math.random() * (endMilliseconds - startMilliseconds) + startMilliseconds;
  await sleep(duration);
}

