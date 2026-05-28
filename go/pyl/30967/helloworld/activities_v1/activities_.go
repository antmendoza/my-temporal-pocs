package activities_v1

import (
	"context"

	"go.temporal.io/sdk/activity"
)

func Activity(ctx context.Context) (string, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("Activity activities_v1")
	return "activities_v1!", nil
}
