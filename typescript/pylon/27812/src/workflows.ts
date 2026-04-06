import { continueAsNew, proxyActivities, sleep, WorkflowInterceptorsFactory } from '@temporalio/workflow';
import {
  OpenTelemetryInboundInterceptor,
  OpenTelemetryInternalsInterceptor,
  OpenTelemetryOutboundInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/workflow';
import type * as activities from './activities';

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '5 minute',
  heartbeatTimeout: '2 seconds',
 //cancellationType: "TRY_CANCEL",
});

// A workflow that simply calls an activity
export async function example(name: string): Promise<string> {
  const activit = [];
  for (let i = 0; i < 200; i++) {
    activit.push(greet(name));
  }

  try {
    await Promise.all(activit);
  }catch (e) {
    console.log(e);
  }

  await sleep('1 minute');

  //await continueAsNew(name);
  return '';
}

// Export the interceptors
export const interceptors: WorkflowInterceptorsFactory = () => ({
  inbound: [new OpenTelemetryInboundInterceptor()],
  outbound: [new OpenTelemetryOutboundInterceptor()],
  internals: [new OpenTelemetryInternalsInterceptor()],
});
