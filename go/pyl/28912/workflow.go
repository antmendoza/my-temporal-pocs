// Package update_with_start_interceptor reproduces the asymmetry between
// SignalWithStartWorkflow and UpdateWithStartWorkflow in a
// ClientOutboundInterceptor.
//
// ClientSignalWithStartWorkflowInput exposes Options *client.StartWorkflowOptions
// directly, so an interceptor can read and mutate the start options freely.
//
// ClientUpdateWithStartWorkflowInput instead carries start parameters inside a
// client.WithStartWorkflowOperation interface whose only concrete implementation
// is the unexported internal.withStartWorkflowOperationImpl. An interceptor
// therefore has no supported way to access or modify the StartWorkflowOptions
// (task queue, memo, search attributes, etc.) for an UpdateWithStartWorkflow call.
//
// See interceptor_test.go for a runnable reproduction.
package update_with_start_interceptor

import (
	"go.temporal.io/sdk/workflow"
)

const (
	TaskQueue  = "update-with-start-interceptor"
	UpdateName = "add-greeting"
	DoneSignal = "done"
)

// GreetingWorkflow collects greetings via updates until it receives a done signal,
// then returns the collected greetings.
func GreetingWorkflow(ctx workflow.Context) ([]string, error) {
	var greetings []string
	if err := workflow.SetUpdateHandler(ctx, UpdateName, func(ctx workflow.Context, greeting string) error {
		greetings = append(greetings, greeting)
		return nil
	}); err != nil {
		return nil, err
	}
	workflow.GetSignalChannel(ctx, DoneSignal).Receive(ctx, nil)
	return greetings, nil
}
