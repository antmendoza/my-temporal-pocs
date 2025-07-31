package _14283

import (
	"context"
	"go.temporal.io/sdk/activity"
	"log"
	"time"
)

type Activities struct {
	Name     string
	Greeting string
}

// GetName Activity.
func (a *Activities) GetName() (string, error) {
	return a.Name, nil
}
func (a *Activities) GetGreeting(ctx context.Context, t int) (string, error) {
	log.Println("GetGreeting called with:", t)

	// Get the worker stop channel
	stopCh := activity.GetWorkerStopChannel(ctx)

	for i := 0; i < 100; i++ {
		select {

		case <-ctx.Done():
			log.Println("Context cancelled.")
			return "cancelled", ctx.Err()

		case <-time.After(1 * time.Second):
			log.Printf("Tick %d/%d\n", i+1, t)

			//		if i == t-1 { // fixed condition: i runs 0..t-1
			//			log.Println("Returning early from GetGreeting")
			//		return "done", nil
			//			}

		case <-stopCh:

			log.Println("Worker is stopping .")
			time.Sleep(1 * time.Second) // Simulate work

		}
	}

	return "end", nil
}

// SayGreeting Activity.
func (a *Activities) SayGreeting() (string, error) {

	time.Sleep(3 * time.Second)
	return "result", nil
}
