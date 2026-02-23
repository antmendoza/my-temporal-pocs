import {executeChild, proxyActivities, WorkflowInterceptorsFactory} from '@temporalio/workflow';
import {
  OpenTelemetryInboundInterceptor,
  OpenTelemetryOutboundInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/workflow';
import type * as activities from './activities';
import {exampleChild} from "./workflows_child";

const { greet } = proxyActivities<typeof activities>({
  startToCloseTimeout: '1 minute',
});

// A workflow that simply calls an activity
export async function example(name: string): Promise<string> {

  await executeChild(exampleChild, {
    args:[name],
    taskQueue: 'interceptors-opentelemetry-example-child',
  })
  return await greet(name);
}

// Export the interceptors
export const interceptors: WorkflowInterceptorsFactory = () => ({
  inbound: [new OpenTelemetryInboundInterceptor()],
  outbound: [new OpenTelemetryOutboundInterceptor()],
});
