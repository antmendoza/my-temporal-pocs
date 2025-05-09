package src

import (
	"context"
	"fmt"
	"go.temporal.io/sdk/temporal"
	"time"

	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/workflow"
)

// Workflow is a standard workflow definition.
// Note that the Workflow and Activity don't need to care that
// their inputs/results are being encrypted/decrypted.
func Workflow(ctx workflow.Context, name string) (string, error) {
	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
	}
	ctx = workflow.WithActivityOptions(ctx, ao)

	logger := workflow.GetLogger(ctx)
	logger.Info("Encrypted Payloads workflow started", "name", name)

	info := map[string]string{
		"name": name,
	}

	var result string
	err := workflow.ExecuteActivity(ctx, Activity, info).Get(ctx, &result)
	if err != nil {
		logger.Error("Activity failed.", "Error", err)
		return "", err
	}

	logger.Info("Encrypted Payloads workflow completed.", "result", result)

	return result, nil
}

func Activity(ctx context.Context, info map[string]string) (string, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("Activity", "info", info)

	name, ok := info["name"]
	if !ok {
		name = "someone"
	}

	// simulate failure after process 1/3 of the tasks
	if activity.GetInfo(ctx).Attempt < 2 {
		logger.Info("Activity failed, will retry...")
		// Activity could return *ApplicationError which is always retryable.
		// To return non-retryable error use temporal.NewNonRetryableApplicationError() constructor.
		return "", temporal.NewNonRetryableApplicationError(
			"some retryable error",
			"SomeType",
			fmt.Errorf("math: square root of negative number"),
			nil)
	}

	return "Hello " + name + "!", nil
}
