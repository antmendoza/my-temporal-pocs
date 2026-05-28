package activities_2

import (
	"context"

	"go.temporal.io/sdk/activity"
)

func Activity(ctx context.Context) (string, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("Activity")
	return "Hello 2!", nil
}
