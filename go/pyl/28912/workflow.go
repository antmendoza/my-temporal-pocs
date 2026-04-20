package update_with_start_interceptor

import (
	"go.temporal.io/sdk/workflow"
)

const (
	TaskQueue  = "update-with-start-interceptor"
	UpdateName = "add-greeting"
	DoneSignal = "done"
)

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
