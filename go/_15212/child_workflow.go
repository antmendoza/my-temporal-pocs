package child_workflow

import (
	"go.temporal.io/sdk/workflow"
	"time"
)

func SampleChildWorkflow(ctx workflow.Context, name string) (string, error) {
	logger := workflow.GetLogger(ctx)
	logger.Info("before timer")

	err_ := workflow.Sleep(ctx, 10*time.Second)

	if err_ != nil {
		logger.Error("Sleep failed.", "Error", err_)
		return "child canceled", nil
	}
	greeting := "Hello " + name + "!"
	logger.Info("after timer")
	return greeting, nil
}
