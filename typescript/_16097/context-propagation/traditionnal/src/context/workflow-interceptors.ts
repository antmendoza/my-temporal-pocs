import {
  ActivityFailure,
  ActivityInput,
  AsyncLocalStorage,
  ContinueAsNewInput,
  DisposeInput,
  GetLogAttributesInput,
  GetMetricTagsInput,
  LocalActivityInput,
  Next,
  proxyLocalActivities,
  QueryInput,
  SignalInput,
  StartChildWorkflowExecutionInput,
  UpdateInput,
  WorkflowExecuteInput,
  WorkflowInboundCallsInterceptor,
  WorkflowInterceptors,
  WorkflowInternalsInterceptor,
  WorkflowOutboundCallsInterceptor,
} from '@temporalio/workflow';
import { MetricTags } from '@temporalio/common';
import { extractContextHeader, injectContextHeader, PropagatedContext } from './context-type';
import type * as activities from '../activities';

const contextStorage = new AsyncLocalStorage<PropagatedContext>();

const { generateNewEncryptedToken } = proxyLocalActivities<typeof activities>({
  startToCloseTimeout: '5 seconds',
});

export function withContext<Ret>(
  extraContext: PropagatedContext | undefined,
  fn: (context: PropagatedContext) => Ret
): Ret {
  if (!extraContext) return fn(getContext());
  const newContext = { ...contextStorage.getStore(), ...extraContext };
  return contextStorage.run(newContext, () => fn(newContext));
}

export function getContext(): PropagatedContext {
  return contextStorage.getStore() ?? {};
}

class ContextWorklfowInterceptor
  implements WorkflowInboundCallsInterceptor, WorkflowOutboundCallsInterceptor, WorkflowInternalsInterceptor
{
  private executionContext: PropagatedContext | undefined;

  async scheduleLocalActivity(
    input: LocalActivityInput,
    next: Next<WorkflowOutboundCallsInterceptor, 'scheduleLocalActivity'>
  ): Promise<unknown> {
    const p = next({
      ...input,
      headers: injectContextHeader(input.headers, getContext()),
    });

    if (input.activityType == 'generateNewEncryptedToken') {
      return await tokenToContext(p, (err) => {
        //console.error(err);
      });
    }

    return await p;
  }

  async scheduleActivity(
    input: ActivityInput,
    next: Next<WorkflowOutboundCallsInterceptor, 'scheduleActivity'>
  ): Promise<unknown> {
    const promise = next({
      ...input,
      headers: injectContextHeader(input.headers, getContext()),
    });

    return await this.checkAndRetryWithNewToken(input, promise,next, (err) => {
      //console.error(err);
    });
  }

  async execute(input: WorkflowExecuteInput, next: Next<WorkflowInboundCallsInterceptor, 'execute'>): Promise<unknown> {
    this.executionContext = extractContextHeader(input.headers);
    return withContext(this.executionContext, () => next(input));
  }

  async handleSignal(input: SignalInput, next: Next<WorkflowInboundCallsInterceptor, 'handleSignal'>): Promise<void> {
    const inboundContext = extractContextHeader(input.headers);
    return withContext({ ...this.executionContext, ...inboundContext }, () => next(input));
  }

  async handleQuery(input: QueryInput, next: Next<WorkflowInboundCallsInterceptor, 'handleQuery'>): Promise<unknown> {
    const inboundContext = extractContextHeader(input.headers);
    return withContext({ ...this.executionContext, ...inboundContext }, () => next(input));
  }

  async handleUpdate(
    input: UpdateInput,
    next: Next<WorkflowInboundCallsInterceptor, 'handleUpdate'>
  ): Promise<unknown> {
    const inboundContext = extractContextHeader(input.headers);
    return withContext({ ...this.executionContext, ...inboundContext }, () => next(input));
  }

  validateUpdate(input: UpdateInput, next: Next<WorkflowInboundCallsInterceptor, 'validateUpdate'>) {
    const inboundContext = extractContextHeader(input.headers);
    return withContext({ ...this.executionContext, ...inboundContext }, () => next(input));
  }

  async startChildWorkflowExecution(
    input: StartChildWorkflowExecutionInput,
    next: Next<WorkflowOutboundCallsInterceptor, 'startChildWorkflowExecution'>
  ): Promise<[Promise<string>, Promise<unknown>]> {
    return await next({
      ...input,
      headers: injectContextHeader(input.headers, getContext()),
    });
  }

  async continueAsNew(
    input: ContinueAsNewInput,
    next: Next<WorkflowOutboundCallsInterceptor, 'continueAsNew'>
  ): Promise<never> {
    return await next({
      ...input,
      headers: injectContextHeader(input.headers, getContext()),
    });
  }

  getLogAttributes(
    input: GetLogAttributesInput,
    next: Next<WorkflowOutboundCallsInterceptor, 'getLogAttributes'>
  ): Record<string, unknown> {
    return next({
      input,
      ...getContext(),
    });
  }

  getMetricTags(input: GetMetricTagsInput, next: Next<WorkflowOutboundCallsInterceptor, 'getMetricTags'>): MetricTags {
    // FIXME: determine how context needs to affect metric tags
    return next(input);
  }

  dispose(input: DisposeInput, next: Next<WorkflowInternalsInterceptor, 'dispose'>): void {
    contextStorage.disable();
    next(input);
  }




  private async checkAndRetryWithNewToken<T>(
  input: ActivityInput,
  p: Promise<T>,
  next: Next<WorkflowOutboundCallsInterceptor, 'scheduleActivity'>,
  onFail: (err: unknown) => void
): Promise<T> {


  try {
    return await p;
  } catch (e) {
    if (e instanceof ActivityFailure && e.cause?.message == 'AuthError') {
      console.log('retrying to get new token');
      //call generateNewEncryptedToken to get new token
      await generateNewEncryptedToken();

      const activity = input.activityType;

      const args = input.args ?? [];

//      return this.scheduleActivity(
 //       input,
 //       next
 //     );
    }
    onFail(e);
    throw e;
  }
}
}

export const interceptors = (): WorkflowInterceptors => {
  const interceptor = new ContextWorklfowInterceptor();
  return {
    inbound: [interceptor],
    outbound: [interceptor],
    internals: [interceptor],
  };
};


async function tokenToContext<T>(p: Promise<T>, onFail: (err: unknown) => void): Promise<T> {


  void p.then((result) => {
    const token = result as string;
    getContext().authToken = token;
    return token;
  });

  try {
    return await p;
  } catch (e) {
    onFail(e);
    throw e;
  }
}
