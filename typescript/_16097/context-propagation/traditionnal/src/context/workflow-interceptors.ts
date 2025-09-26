import {
  ActivityFailure,
  ActivityInput,
  AsyncLocalStorage,
  ContinueAsNewInput,
  DisposeInput,
  GetLogAttributesInput,
  GetMetricTagsInput,
  LocalActivityInput,
  Next, proxyActivities, proxyLocalActivities,
  QueryInput, scheduleActivity,
  SignalInput,
  StartChildWorkflowExecutionInput,
  UpdateInput,
  WorkflowExecuteInput,
  WorkflowInboundCallsInterceptor,
  WorkflowInterceptors,
  WorkflowInternalsInterceptor,
  WorkflowOutboundCallsInterceptor
} from '@temporalio/workflow';
import { MetricTags } from '@temporalio/common';
import { extractContextHeader, injectContextHeader, PropagatedContext } from './context-type';
import { ApplicationFailure } from '@temporalio/workflow';
import type * as activities from '../activities';

const contextStorage = new AsyncLocalStorage<PropagatedContext>();


const {
  generateNewEncryptedToken
} = proxyLocalActivities<typeof activities>({
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

  async scheduleActivity(
    input: ActivityInput,
    next: Next<WorkflowOutboundCallsInterceptor, 'scheduleActivity'>
  ): Promise<unknown> {


    const promise = next({
      ...input,
      headers: injectContextHeader(input.headers, getContext()),
    });


    return checkAndRetryWithNewToken(input, promise, (err) => {
      console.error(err);
    });

    return await promise;



  }

  async scheduleLocalActivity(
    input: LocalActivityInput,
    next: Next<WorkflowOutboundCallsInterceptor, 'scheduleLocalActivity'>
  ): Promise<unknown> {
    const p = next({
      ...input,
      headers: injectContextHeader(input.headers, getContext()),
    });

    if (input.activityType == "generateNewEncryptedToken") {
      return tokenToContext(p, (err) => {
        console.error('Local activity failed, refreshing context', err);
      });
    }

    return await p;

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
}

export const interceptors = (): WorkflowInterceptors => {
  const interceptor = new ContextWorklfowInterceptor();
  return {
    inbound: [interceptor],
    outbound: [interceptor],
    internals: [interceptor],
  };
};



function checkAndRetryWithNewToken<T>(input: ActivityInput, p: Promise<T>, onFail: (e: unknown) => void): Promise<T> {

  return p.catch(async (e) => {
    if (e instanceof ActivityFailure && e.cause?.message == 'AuthError') {
      console.log('retrying with new token');

      const activity = input.activityType;

      //call generateNewEncryptedToken to get new token
      await generateNewEncryptedToken();


      //retry original activity with new token in context
      //TODO

    }
    onFail(e);
    throw e;
  });
}


function tokenToContext<T>(p: Promise<T>, onFail: (e: unknown) => void): Promise<T> {

  void p.then((result => {
    const token = result as string;
    getContext().authToken = token;
    return token;
  }));

  return p.catch((e) => {
    onFail(e);
    throw e;
  });
}
