package activities_33

import (
	"context"

	"go.temporal.io/sdk/activity"
)

func Activity(ctx context.Context) (string, error) {
	logger := activity.GetLogger(ctx)
	logger.Info("Activity")
	return "Hello 33 vis!", nil
}
