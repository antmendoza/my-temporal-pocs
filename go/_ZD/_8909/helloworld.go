package _8909

import (
	"context"
	"time"

	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/workflow"
)

type StarterWorkflowInput struct {
	EvictionInterval *time.Duration `json:"eviction_interval"`
}

func (in *StarterWorkflowInput) evictionInterval() time.Duration {
	if in.EvictionInterval == 0 {
		return defaultEvictionInterval
	}
	return in.EvictionInterval
}

var defaultEvictionInterval time.Duration = time.Second * 10

// Workflow is a Hello World workflow definition.
func Workflow(ctx workflow.Context, in StarterWorkflowInput) error {
	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
	}
	ctx = workflow.WithActivityOptions(ctx, ao)

	logger := workflow.GetLogger(ctx)
	logger.Info("HelloWorld workflow started", "name", "name")

	var result string
	_ = workflow.ExecuteActivity(ctx, Activity, "name").Get(ctx, &result)

	logger.Info("HelloWorld workflow completed.", "result", result)

	_ = workflow.Sleep(ctx, 5000*time.Millisecond)

	if true {
		return starterContinueAsNewError(ctx, in.evictionInterval())
	}

	return nil
}

func Activity(ctx context.Context, name string) (string, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("Activity", "name", name)
	return "Hello " + name + "!", nil
}

func starterContinueAsNewError(ctx workflow.Context, evictionInterval time.Duration) error {
	name := workflow.GetInfo(ctx).WorkflowType.Name
	return workflow.NewContinueAsNewError(ctx, name, &StarterWorkflowInput{
		EvictionInterval: &evictionInterval,
	})
}
