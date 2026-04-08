package _14283

import (
	"time"

	"go.temporal.io/sdk/workflow"
)

func GreetingSample(ctx workflow.Context) (st string, err error) {

	ao := workflow.LocalActivityOptions{
		StartToCloseTimeout: 20 * time.Second,
	}

	ctxLA := workflow.WithLocalActivityOptions(ctx, ao)

	//cancellable context
	//	ctxLA, cancel := workflow.WithCancel(ctxLA)

	var a *Activities
	var result1 string

	//	cancel()

	err = workflow.ExecuteLocalActivity(ctxLA, a.GetGreeting, 6).Get(ctxLA, &result1)
	if err != nil {
		return
	}

	return "done", nil
}
