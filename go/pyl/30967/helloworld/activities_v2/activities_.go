package activities_v2

import (
	"context"

	"go.temporal.io/sdk/activity"
)

func Activity(ctx context.Context) (string, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("Activity activities_v2")
	return "activities_v2", nil
}
