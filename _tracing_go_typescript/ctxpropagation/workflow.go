package ctxpropagation

import (
	"fmt"
	"go.temporal.io/sdk/workflow"
	"time"
)

// CtxPropWorkflow workflow definition
func CtxPropWorkflow(ctx workflow.Context) (err error) {
	tsTaskQueueName := "ts-taskqueue"
	tsAo := workflow.ActivityOptions{
		StartToCloseTimeout: 5 * time.Second,
		TaskQueue:           tsTaskQueueName,
	}
	tsCtx := workflow.WithActivityOptions(ctx, tsAo)

	goAo := workflow.ActivityOptions{
		StartToCloseTimeout: 5 * time.Second,
	}
	goCtx := workflow.WithActivityOptions(ctx, goAo)

	if val := ctx.Value(PropagateKey); val != nil {
		vals := val.(Values)
		workflow.GetLogger(ctx).Info("custom context propagated to workflow", vals.Key, vals.Value)
	}

	var values Values
	if err = workflow.ExecuteActivity(goCtx, SampleActivity).Get(goCtx, &values); err != nil {
		workflow.GetLogger(ctx).Error("CtxPropWorkflow failed.", "Error", err)
		return err
	}
	workflow.GetLogger(ctx).Info("context propagated to activity", values.Key, values.Value)

	workflow.ExecuteActivity(goCtx, SampleActivity).Get(goCtx, &values)

	//Activity running in a TS worker
	var tsActivityResult string
	if err = workflow.ExecuteActivity(tsCtx, "activity1").Get(tsCtx, &tsActivityResult); err != nil {
		workflow.GetLogger(ctx).Error("CtxPropWorkflow failed.", "Error", err)
		return err
	}
	workflow.GetLogger(ctx).Info("tsActivityResult", tsActivityResult)

	ctxChild := workflow.WithChildOptions(ctx, workflow.ChildWorkflowOptions{
		WorkflowID: fmt.Sprintf("%s%s", workflow.GetInfo(ctx).WorkflowExecution.ID, "::child"),
		TaskQueue:  tsTaskQueueName,
	})

	childWorkflow := workflow.ExecuteChildWorkflow(ctxChild, "ts_workflow", "input")

	// Wait for child to complete
	_ = childWorkflow.Get(ctx, nil)

	workflow.GetLogger(ctx).Info("CtxPropWorkflow completed.")
	return nil
}
