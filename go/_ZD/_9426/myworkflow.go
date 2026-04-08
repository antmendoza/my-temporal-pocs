package helloworld

import (
	"context"
	"fmt"
	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/workflow"
	"sync"
	"time"
)

type WorkflowInput struct {
	NumberOfActivitiesInParent           int
	NumberOfExternalWorkflowsPerActivity int
}

type MyActivity struct {
	WorkflowClient client.Client
}

func (a *MyActivity) GetSubscribers_Sleep_10Ms() error {
	time.Sleep(time.Millisecond * 10)

	return nil
}

func (a *MyActivity) DispatchBatch_createExternalWorkflows(ctx context.Context, input WorkflowInput, activityNumber int) error {

	var wg sync.WaitGroup

	for i := 0; i < input.NumberOfExternalWorkflowsPerActivity; i++ {

		workflowId := activity.GetInfo(ctx).WorkflowExecution.ID
		workflowOptions := client.StartWorkflowOptions{
			ID:        fmt.Sprintf("%s%s:%d:%d", workflowId, "::external-", activityNumber, i),
			TaskQueue: "hello-world",
		}

		go func() {
			wg.Add(1)
			wr, _ := a.WorkflowClient.ExecuteWorkflow(context.Background(), workflowOptions, SendPlayToSubscriber, input)

			defer wg.Done()
			_ = a.WorkflowClient.GetWorkflow(ctx, wr.GetID(), wr.GetRunID()).Get(ctx, nil)
			//Ignore error

		}()

	}

	wg.Wait()

	return nil

}

func (a *MyActivity) SendNotification_Sleep_10Ms() error {
	time.Sleep(time.Millisecond * 10)
	return nil

}

func (a *MyActivity) DoSomething_Speep_10Ms() error {
	time.Sleep(time.Millisecond * 10)
	return nil

}

func SendPlay(ctx workflow.Context, input WorkflowInput) (string, error) {

	logger := workflow.GetLogger(ctx)
	logger.Info("HelloWorld workflow started", "name", input)

	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 1 * time.Minute,
	}
	ctxAo := workflow.WithActivityOptions(ctx, ao)

	var a *MyActivity

	for i := 0; i < input.NumberOfActivitiesInParent; i++ {

		//TODO This could be a local activity
		_ = workflow.ExecuteActivity(ctxAo, a.GetSubscribers_Sleep_10Ms).Get(ctxAo, nil)

		_ = workflow.ExecuteActivity(ctxAo, a.DispatchBatch_createExternalWorkflows, input, i).Get(ctxAo, nil)

	}

	return "done", nil
}

func SendPlayToSubscriber(ctx workflow.Context, input WorkflowInput) (string, error) {

	logger := workflow.GetLogger(ctx)
	logger.Info("HelloWorld workflow started", "name", input)

	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 1 * time.Second,
	}
	ctxAo := workflow.WithActivityOptions(ctx, ao)

	ctxChild := workflow.WithChildOptions(ctx, workflow.ChildWorkflowOptions{
		WorkflowID: fmt.Sprintf("%s%s", workflow.GetInfo(ctx).WorkflowExecution.ID, "::child"),
	})

	var a *MyActivity

	childWorkflow := workflow.ExecuteChildWorkflow(ctxChild, SendPlayToSubscriberChild, input)

	//TODO This could be local activities
	_ = workflow.ExecuteActivity(ctxAo, a.DoSomething_Speep_10Ms).Get(ctxAo, nil)
	_ = workflow.ExecuteActivity(ctxAo, a.DoSomething_Speep_10Ms).Get(ctxAo, nil)
	_ = workflow.ExecuteActivity(ctxAo, a.DoSomething_Speep_10Ms).Get(ctxAo, nil)

	// Wait for child to complete
	_ = childWorkflow.Get(ctx, nil)

	return "done", nil
}

func SendPlayToSubscriberChild(ctx workflow.Context, input WorkflowInput) (string, error) {

	logger := workflow.GetLogger(ctx)
	logger.Info("HelloWorld workflow started", "name", input)

	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 1 * time.Second,
	}
	ctxAo := workflow.WithActivityOptions(ctx, ao)

	var a *MyActivity

	//TODO This could be local activities
	_ = workflow.ExecuteActivity(ctxAo, a.SendNotification_Sleep_10Ms).Get(ctxAo, nil)
	_ = workflow.ExecuteActivity(ctxAo, a.SendNotification_Sleep_10Ms).Get(ctxAo, nil)
	_ = workflow.ExecuteActivity(ctxAo, a.SendNotification_Sleep_10Ms).Get(ctxAo, nil)
	//Ignore error

	return "done", nil
}
