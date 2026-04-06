import { Context } from '@temporalio/activity';
import { ActivityInboundCallsInterceptor, ActivityExecuteInput, Next } from '@temporalio/worker';

/**
 * Activity inbound interceptor that logs RespondActivityHeartbeat failures.
 *
 * Note: The TypeScript SDK's NativeConnection (Rust core) does not expose gRPC-level
 * interceptors for worker-side calls, so heartbeat() is fire-and-forget at the
 * TypeScript layer. Heartbeat failures surface through the cancellationSignal when
 * the server rejects the heartbeat (e.g. activity not found, workflow cancelled).
 *
 * This interceptor:
 *  - Patches ctx.heartbeat() to log each heartbeat sent
 *  - Listens on cancellationSignal, which fires when a heartbeat fails on the server
 */
export class HeartbeatLoggingInterceptor implements ActivityInboundCallsInterceptor {
  async execute(input: ActivityExecuteInput, next: Next<ActivityInboundCallsInterceptor, 'execute'>): Promise<unknown> {
    const ctx = Context.current();

    // Wrap the heartbeat function to log each send attempt
    const originalHeartbeat = ctx.heartbeat.bind(ctx);
    (ctx as any).heartbeat = (details?: unknown) => {
     // console.log(
     //   new Date().getTime() + '[HeartbeatInterceptor] Sending RespondActivityHeartbeat...' + ctx.info.activityId
     // );
      originalHeartbeat(details);
    };




    // cancellationSignal is aborted when the server rejects a heartbeat (e.g. activity
    // timed out or workflow was terminated), which is the primary failure signal we have.
    ctx.cancellationSignal.addEventListener('abort', () => {
      console.error( new Date().getTime() +
        '[HeartbeatInterceptor] RespondActivityHeartbeat failed / activity cancelled:',
        ctx.info.activityType, // Activity type for debugging
        ctx.info.activityId, // Activity type for debugging
        //ctx.cancellationSignal.reason
      );
    });

    return next(input);
  }
}