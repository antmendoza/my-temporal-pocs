package myapp

import (
	"context"
	"fmt"
	"go.temporal.io/sdk/activity"
	"time"
)

type YourActivityParam struct {
	ActivityParamX string
	Time           time.Duration
}

type YourActivityObject struct {
}

// ...
func (a *YourActivityObject) YourActivityDefinition(ctx context.Context, param YourActivityParam) (string, error) {

	logger := activity.GetLogger(ctx)

	elapsedDuration := time.Nanosecond
	for elapsedDuration < param.Time {
		time.Sleep(time.Second)
		elapsedDuration += time.Second

		// record heartbeat every second to check if we are been cancelled
		activity.RecordHeartbeat(ctx, elapsedDuration)

		time.Sleep(1 * time.Second)

		logger.Info("heartbeat " + param.ActivityParamX)

		select {
		case <-ctx.Done():
			// We have been cancelled.
			msg := fmt.Sprintf("Branch %d is cancelled.", param.ActivityParamX)

			time.Sleep(5 * time.Second)

			return msg, ctx.Err()
			//or return msg, nil
			// to return a normal value
		default:
			// We are not cancelled yet.
		}
	}

	return param.ActivityParamX, nil
}
