import { CancelledFailure, Context } from '@temporalio/activity';
import { ActivityExecuteInput, ActivityInboundCallsInterceptor, Next } from '@temporalio/worker';

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

    this.subscribeAndPrintCancellationSignal(ctx);


    //return next(input);
    return await this.executeActivity(input, next, ctx);

  }
  private async executeActivity(
    input: ActivityExecuteInput,
    next: Next<ActivityInboundCallsInterceptor, 'execute'>,
    ctx: Context
  ) {
    try {
      return await next(input);
    } catch (e) {
      let msg = '';
      if (e instanceof CancelledFailure) {
        msg = e.message;
      }
      console.error(
        new Date().getTime() + '[HeartbeatInterceptor] Error:',
        'Activity type:' + ctx.info.activityType,
        'workflow runId: ' + ctx.info.workflowExecution.runId,
        msg
      );

      throw e;
    }
  }

  private subscribeAndPrintCancellationSignal(ctx: Context) {
    // cancellationSignal is aborted when the server rejects a heartbeat (e.g. activity
    // timed out or workflow was terminated), which is the primary failure signal we have.
    ctx.cancellationSignal.addEventListener('abort', () => {
      console.error(
        '[HeartbeatInterceptor] Activity cancelled:',
        ctx.info.activityType,
        ctx.info.activityId,
        ctx.cancellationSignal.reason
      );
    });
  }
}
