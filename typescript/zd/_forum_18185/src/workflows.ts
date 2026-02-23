import { proxyActivities } from '@temporalio/workflow';
// Only import the activity types
import type * as activities from './activities';

const { alpha, beta, gamma, delta, epsilon } = proxyActivities<typeof activities>({
  startToCloseTimeout: '10 minutes',
  scheduleToStartTimeout: '10 minutes',
  scheduleToCloseTimeout: '10 minutes',
  taskQueue: 'default',
  retry: {
    maximumInterval: '10 minutes',
  },
});


export async function runSimpleDAG(): Promise<string> {
  console.log('Workflow starting');

  // Step 1: Alpha runs independently
  console.log(('About to call alpha'));
  const alphaResult = await alpha();
  console.log('Alpha completed', { result: alphaResult });

  console.log(('About to call beta'));
  // Test: Just schedule Beta and see if it works
  const betaResult = await beta(alphaResult);
  console.log('Beta completed', { result: betaResult });

  console.log('About to call gamma')
  // If we get here, the issue is not with Beta
  // Let's test Gamma
  const gammaResult = await gamma(alphaResult);
  console.log('Gamma completed', { result: gammaResult });

  console.log(('About to call delta'));
  // If we get here, the issue is not with Gamma either
  // Let's test Delta
  const deltaResult = await delta(gammaResult);
  console.log('Delta completed', { result: deltaResult });

  console.log('About to call epsilon')
  // Finally test Epsilon
  await epsilon(betaResult, deltaResult);
  console.log(('Epsilon completed'));

  console.log(('Workflow completed successfully'));

  return "done";
}

