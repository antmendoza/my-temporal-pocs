package child_workflow

import (
	"go.temporal.io/sdk/workflow"
	"time"
)

func SampleChildWorkflow(ctx workflow.Context, name string) (string, error) {
	logger := workflow.GetLogger(ctx)
	logger.Info("before timer")

	err := workflow.Sleep(ctx, 10*time.Second)

	if err != nil {
		logger.Error("Sleep failed.", "Error", err)
		return "", err
	}
	logger.Info("after timer")
	greeting := "Hello " + name + "!"
	return greeting, nil
}
