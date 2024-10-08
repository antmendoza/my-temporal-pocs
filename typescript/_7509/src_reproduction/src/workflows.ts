import { proxyActivities, WorkflowInterceptorsFactory } from '@temporalio/workflow';
import {
  OpenTelemetryInboundInterceptor,
  OpenTelemetryOutboundInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/workflow';
// @@@SNIPSTART typescript-activity-deps-workflow
import type { createActivities } from './activities';

// Note usage of ReturnType<> generic since createActivities is a factory function
const { greet} = proxyActivities<ReturnType<typeof createActivities>>({
  startToCloseTimeout: '30 seconds',
});

// A workflow that simply calls an activity
export async function example(name: string): Promise<string> {
  return await greet(name);
}

// Export the interceptors
export const interceptors: WorkflowInterceptorsFactory = () => ({
  inbound: [new OpenTelemetryInboundInterceptor()],
  outbound: [new OpenTelemetryOutboundInterceptor()],
});
