package helloworld

import (
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/workflow"
	"time"
)

type WorkflowInput struct {
	NumberOfActivitiesInParent           int
	NumberOfExternalWorkflowsPerActivity int
}

type MyActivity struct {
	WorkflowClient client.Client
}

func (a *MyActivity) DoSomething_Speep_10Ms() error {
	time.Sleep(time.Millisecond * 10)
	return nil

}

func SendPlay(ctx workflow.Context, input WorkflowInput) (string, error) {

	logger := workflow.GetLogger(ctx)
	logger.Info("HelloWorld workflow started", "name", input)

	return "done", nil
}
