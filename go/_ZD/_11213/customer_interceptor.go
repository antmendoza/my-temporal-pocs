package _11213

import (
	"context"
	"fmt"
	"go.temporal.io/sdk/interceptor"
	"go.temporal.io/sdk/workflow"
)

type LoggingInterceptor struct {
	interceptor.WorkerInterceptorBase
}

func (l LoggingInterceptor) InterceptActivity(ctx context.Context, next interceptor.ActivityInboundInterceptor) interceptor.ActivityInboundInterceptor {
	return &activityLoggingInbound{next: next}
}

func (l LoggingInterceptor) InterceptWorkflow(ctx workflow.Context, next interceptor.WorkflowInboundInterceptor) interceptor.WorkflowInboundInterceptor {
	return &workflowLoggingInbound{next: next}
}

type activityLoggingInbound struct {
	interceptor.ActivityInboundInterceptorBase
	next interceptor.ActivityInboundInterceptor
}

func (a *activityLoggingInbound) ExecuteActivity(ctx context.Context, in *interceptor.ExecuteActivityInput) (interface{}, error) {
	result, err := a.next.ExecuteActivity(ctx, in)
	if err != nil {
		fmt.Printf("Activity execution error: %v\n", err)
	}
	return result, err
}

type workflowLoggingInbound struct {
	interceptor.WorkflowInboundInterceptorBase
	next interceptor.WorkflowInboundInterceptor
}

func (w *workflowLoggingInbound) ExecuteWorkflow(ctx workflow.Context, in *interceptor.ExecuteWorkflowInput) (interface{}, error) {
	result, err := w.next.ExecuteWorkflow(ctx, in)
	if err != nil {
		fmt.Printf("Activity execution error: %v\n", err)
	}
	return result, err
}
