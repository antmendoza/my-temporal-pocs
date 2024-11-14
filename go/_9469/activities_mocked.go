package activity_schedule_to_close

import (
	"context"
	"fmt"
	"go.temporal.io/sdk/activity"
	"time"
)

// @@@SNIPSTART samples-go-dependency-sharing-activities
type ActivitiesMocked struct {
	Name       string
	Greeting   string
	Activities Activities
}

// GetGreeting Activity.
func (a *ActivitiesMocked) GetGreeting() (string, error) {
	return a.Greeting, nil
}

// @@@SNIPEND

// GetName Activity.
func (a *ActivitiesMocked) GetName(ctx context.Context) (string, error) {
	if activity.GetInfo(ctx).Attempt == 1 {
		time.Sleep(4 * time.Second)
	}
	return "mocked_activity", nil
}

// SayGreeting Activity.
func (a *ActivitiesMocked) SayGreeting(greeting string, name string) (string, error) {
	result := fmt.Sprintf("Greeting: %s %s!\n", greeting, name)
	return result, nil
}
