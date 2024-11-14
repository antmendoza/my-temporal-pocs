import {WorkflowInterceptorsFactory} from '@temporalio/workflow';
import {
    OpenTelemetryInboundInterceptor,
    OpenTelemetryOutboundInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/workflow';


// Export the interceptors
export const interceptors: WorkflowInterceptorsFactory = () => ({
    inbound: [new OpenTelemetryInboundInterceptor()],
    outbound: [new OpenTelemetryOutboundInterceptor()],
});
