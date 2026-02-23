import { sleep } from '@temporalio/activity';

export async function alpha(): Promise<string> {
  const sleepTime = Math.floor(Math.random() * 10000)
  await sleep(sleepTime)
  return `Hello from Alpha! (slept ${sleepTime}ms)`
}

async function extracted() {
  const sleepTime = Math.floor(Math.random() * 10000);
  await sleep(sleepTime);
  return sleepTime;
}

export async function beta(alphaResult: string): Promise<string> {
  const sleepTime = await extracted();

  return `Hello from Beta! (slept ${sleepTime}ms) - Got: ${alphaResult}`;
}

export async function gamma(alphaResult: string): Promise<string> {
  const sleepTime = await extracted();

  return `Hello from gamma! (slept ${sleepTime}ms)`
}


export async function delta(alphaResult: string): Promise<string> {
  const sleepTime = await extracted();
  return `Hello from delta! (slept ${sleepTime}ms)`
}

export async function epsilon(alphaResult: string, deltaResult: string): Promise<string> {
  const sleepTime = await extracted();
  return `Hello from epsilon! (slept ${sleepTime}ms)`
}


