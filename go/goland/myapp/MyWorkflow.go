package myapp

import (
	"errors"
	"fmt"
	"go.temporal.io/sdk/workflow"
	"time"
)

type WorkflowRequest struct {
	NumIterations int
	Name          string
}

func (w *WorkflowRequest) create() WorkflowRequest {
	return WorkflowRequest{
		NumIterations: 2,
		Name:          "int",
	}
}

func (w WorkflowRequest) toString() string {
	return fmt.Sprintf("%s %d", w.Name, w.NumIterations)
}

func MyWorkflow(ctx workflow.Context, input WorkflowRequest) (WorkflowRequest, error) {

	childCtx, cancelHandler := workflow.WithCancel(ctx)

	var signal string
	signalChan := workflow.GetSignalChannel(ctx, "my-signal")
	workflow.Go(ctx, func(ctx workflow.Context) {
		for {
			selector := workflow.NewSelector(ctx)
			selector.AddReceive(signalChan, func(c workflow.ReceiveChannel, more bool) {
				c.Receive(ctx, &signal)
			})
			selector.Select(ctx)
		}
	})

	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 2 * time.Minute,
		HeartbeatTimeout:    4 * time.Second,
		WaitForCancellation: true, // Wait for cancellation to complete.
	}
	childCtx = workflow.WithActivityOptions(childCtx, ao)

	// Use a nil struct pointer to call Activities that are part of a struct.
	var a *YourActivityObject
	// Execute the Activity and wait for the result.
	var activityResult string
	err := workflow.ExecuteActivity(childCtx, a.YourActivityDefinition, YourActivityParam{ActivityParamX: "1"}).Get(ctx, &activityResult)
	if err != nil {
		errors.Unwrap(err)
		return WorkflowRequest{}, err
	}

	selector := workflow.NewSelector(childCtx)

	result1 := workflow.ExecuteActivity(childCtx, a.YourActivityDefinition, YourActivityParam{ActivityParamX: "2", Time: 10 * time.Second})
	result2 := workflow.ExecuteActivity(childCtx, a.YourActivityDefinition, YourActivityParam{ActivityParamX: "3", Time: 2 * time.Second})

	pendingFutures := []workflow.Future{result1, result2}

	selector.AddFuture(result1, func(f workflow.Future) {
		var activityResult1 string
		result1.Get(childCtx, &activityResult1)
		fmt.Println(activityResult1)

	}).AddFuture(result2, func(f workflow.Future) {

		var activityResult1 string
		result2.Get(childCtx, &activityResult1)
		fmt.Println(activityResult1)

	})

	fmt.Println("before selector.select")

	selector.Select(ctx)
	//	selector.Select(ctx)

	cancelHandler()

	workflow.Sleep(ctx, 1*time.Second)

	//Wait for pending futures to complete
	for _, f := range pendingFutures {
		_ = f.Get(ctx, nil)
	}

	fmt.Println("after selector.select")

	return input, nil
}
