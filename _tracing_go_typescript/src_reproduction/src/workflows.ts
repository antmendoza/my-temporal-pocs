import { proxyActivities, WorkflowInterceptorsFactory } from '@temporalio/workflow';
import {
  OpenTelemetryInboundInterceptor, OpenTelemetryInternalsInterceptor,
  OpenTelemetryOutboundInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/workflow';
// @@@SNIPSTART typescript-activity-deps-workflow
import type { createActivities } from './activities';

// Note usage of ReturnType<> generic since createActivities is a factory function
const { activity1} = proxyActivities<ReturnType<typeof createActivities>>({
  startToCloseTimeout: '30 seconds',
});

// A workflow that simply calls an activity
export async function ts_workflow(name: string): Promise<string> {
  return await activity1(name);
}

// Export the interceptors
export const interceptors: WorkflowInterceptorsFactory = () => ({
  inbound: [new OpenTelemetryInboundInterceptor()],
  outbound: [new OpenTelemetryOutboundInterceptor()],
  internals: [new OpenTelemetryInternalsInterceptor()],
});