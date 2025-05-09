import {AsyncLocalStorage, WorkflowInterceptorsFactory} from '@temporalio/workflow';
import {
  ActivityInput,
  ContinueAsNewInput,
  GetLogAttributesInput,
  LocalActivityInput,
  Next,
  QueryInput,
  SignalInput,
  StartChildWorkflowExecutionInput,
  UpdateInput,
  WorkflowExecuteInput,
  WorkflowInboundCallsInterceptor,
  WorkflowInterceptors,
  WorkflowOutboundCallsInterceptor,
} from '@temporalio/workflow';
import { extractContextHeader, injectContextHeader, PropagatedContext } from './context-type';
import {
  OpenTelemetryInboundInterceptor,
  OpenTelemetryOutboundInterceptor
} from "@temporalio/interceptors-opentelemetry";

const contextStorage = new AsyncLocalStorage<PropagatedContext>();

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

class ContextWorklfowInterceptor implements WorkflowInboundCallsInterceptor, WorkflowOutboundCallsInterceptor {
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
    return await next({
      ...input,
      headers: injectContextHeader(input.headers, getContext()),
    });
  }
  async scheduleLocalActivity(
    input: LocalActivityInput,
    next: Next<WorkflowOutboundCallsInterceptor, 'scheduleLocalActivity'>
  ): Promise<unknown> {
    return await next({
      ...input,
      headers: injectContextHeader(input.headers, getContext()),
    });
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

  // Note that we do not propagate context to signals sent from this workflow, as this might create
  // confusion in some cases (i.e. in the signal handler of the target workflow, context values
  // coming from _this workflow_ would override context values defined when that workflow was sent).
  // If that's the intended behavior, simply add intercept the signalWorkflow method.

  getLogAttributes(
    input: GetLogAttributesInput,
    next: Next<WorkflowOutboundCallsInterceptor, 'getLogAttributes'>
  ): Record<string, unknown> {
    return next({
      input,
      ...getContext(),
    });
  }
}

export const interceptors = (): WorkflowInterceptors => {
  const interceptor = new ContextWorklfowInterceptor();
  return {
    inbound: [new OpenTelemetryInboundInterceptor(), interceptor],
    outbound: [ new OpenTelemetryOutboundInterceptor(), interceptor],
  };
};


