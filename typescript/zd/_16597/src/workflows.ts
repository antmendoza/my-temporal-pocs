import { proxyActivities, WorkflowInterceptorsFactory } from '@temporalio/workflow';
import {
  OpenTelemetryInboundInterceptor,
  OpenTelemetryOutboundInterceptor,
  OpenTelemetryInternalsInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/workflow';
import type * as activities from './activities';

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '1 minute',
});

// A workflow that simply calls an activity
export async function example(name: string): Promise<string> {

  await greet(name);
  await greet(name);
  return await greet(name);
}

