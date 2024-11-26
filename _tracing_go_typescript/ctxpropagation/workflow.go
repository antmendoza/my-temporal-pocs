package ctxpropagation

import (
	"go.temporal.io/sdk/workflow"
	"time"
)

// CtxPropWorkflow workflow definition
func CtxPropWorkflow(ctx workflow.Context) (err error) {
	tsAo := workflow.ActivityOptions{
		StartToCloseTimeout: 5 * time.Second,
		TaskQueue:           "ts-taskqueue",
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
		workflow.GetLogger(ctx).Error("Workflow failed.", "Error", err)
		return err
	}
	workflow.GetLogger(ctx).Info("context propagated to activity", values.Key, values.Value)

	workflow.ExecuteActivity(goCtx, SampleActivity).Get(goCtx, &values)

	//Activity running in a TS worker
	var tsActivityResult string
	if err = workflow.ExecuteActivity(tsCtx, "activity1").Get(tsCtx, &tsActivityResult); err != nil {
		workflow.GetLogger(ctx).Error("Workflow failed.", "Error", err)
		return err
	}
	workflow.GetLogger(ctx).Info("tsActivityResult", tsActivityResult)

	workflow.GetLogger(ctx).Info("Workflow completed.")
	return nil
}
