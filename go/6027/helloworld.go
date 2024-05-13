package helloworld

import (
	"context"
	"go.temporal.io/sdk/temporal"
	"time"

	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/workflow"

	// TODO(cretz): Remove when tagged
	_ "go.temporal.io/sdk/contrib/tools/workflowcheck/determinism"
)

// Workflow is a Hello World workflow definition.
func Workflow(ctx workflow.Context, name string) (string, error) {
	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
	}
	ctx = workflow.WithActivityOptions(ctx, ao)

	logger := workflow.GetLogger(ctx)
	logger.Info("HelloWorld workflow started", "name", name)

	var result string
	err := workflow.ExecuteActivity(ctx, Activity, name).Get(ctx, &result)
	if err != nil {
		logger.Error("Activity failed.", "Error", err)
		return "", err
	}

	logger.Info("HelloWorld workflow completed.", "result", result)

	return result, nil
}

func Activity(ctx context.Context, name string) (string, error) {

	logger := activity.GetLogger(ctx)
	go func() {
		for i := 0; i < 10; i++ {
			time.Sleep(1 * time.Second)
			logger.Info("Running...")

		}
	}()
	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()
	for {
		select {
		case <-ticker.C:
			activity.RecordHeartbeat(ctx)
		case <-activity.GetWorkerStopChannel(ctx):
			logger.Info("Activity clean up")

			time.Sleep(2 * time.Second)
			logger.Info("Activity clean up")

			return "", temporal.NewApplicationError("some retryable error", "SomeType")
		}
	}

	logger.Info("Activity", "name", name)
	return "Hello " + name + "!", nil
}
