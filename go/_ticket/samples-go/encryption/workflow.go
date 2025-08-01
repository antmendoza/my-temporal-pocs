package encryption

import (
	"context"
	"time"

	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/workflow"
)

// Workflow is a standard workflow definition.
// Note that the Workflow and Activity don't need to care that
// their inputs/results are being encrypted/decrypted.
func Workflow(ctx workflow.Context, name string) (string, error) {
	ao := workflow.LocalActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
	}
	ctx = workflow.WithLocalActivityOptions(ctx, ao)

	logger := workflow.GetLogger(ctx)
	logger.Info("Encrypted Payloads workflow started", "name", name)

	info := map[string]string{
		"name": name,
	}

	var result string
	err := workflow.ExecuteLocalActivity(ctx, MyActivity, info).Get(ctx, &result)
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

	return "Hello " + name + "!", nil
}

func MyActivity(ctx context.Context, info map[string]string) (string, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("Activity", "info", info)

	name, ok := info["name"]
	if !ok {
		name = "someone"
	}

	return "Hello 33 " + name + "!", nil
}
