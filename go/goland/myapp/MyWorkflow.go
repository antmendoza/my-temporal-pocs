package myapp

import (
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

	// Set the options for the Activity Execution.
	// Either StartToClose Timeout OR ScheduleToClose is required.
	// Not specifying a Task Queue will default to the parent Workflow Task Queue.
	activityOptions := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
	}
	ctx = workflow.WithActivityOptions(ctx, activityOptions)
	activityParam := YourActivityParam{}
	// Use a nil struct pointer to call Activities that are part of a struct.
	var a *YourActivityObject
	// Execute the Activity and wait for the result.
	var activityResult YourActivityResultObject
	err := workflow.ExecuteActivity(ctx, a.YourActivityDefinition, activityParam).Get(ctx, &activityResult)
	if err != nil {
		return WorkflowRequest{}, err
	}

	return input, nil
}
