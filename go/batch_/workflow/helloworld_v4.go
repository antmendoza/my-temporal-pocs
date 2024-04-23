package workflow

import (
	"fmt"
	"go.temporal.io/api/enums/v1"
	"go.temporal.io/sdk/temporal"
	"go.temporal.io/sdk/workflow"
	"time"
)

func ParentWorkflow_V4(ctx workflow.Context, elements []int, childWFPerBatch int) error {

	logger := workflow.GetLogger(ctx)
	logger.Info("Parent workflow START", "WorkflowExecution.id", workflow.GetInfo(ctx).WorkflowExecution.ID, "OriginalRunID", workflow.GetInfo(ctx).OriginalRunID)

	if workflow.IsReplaying(ctx) {
		fmt.Println("Parent replaying_V3...... " + workflow.GetInfo(ctx).OriginalRunID)
	}

	pendingTests := workflow.NewSelector(ctx)

	for i := 0; i < childWFPerBatch; i++ {

		childWorkflowCtx := workflow.WithChildOptions(ctx, workflow.ChildWorkflowOptions{
			TaskQueue:           "hello-child",
			ParentClosePolicy:   enums.PARENT_CLOSE_POLICY_ABANDON,
			WorkflowTaskTimeout: time.Second * 30,
			RetryPolicy: &temporal.RetryPolicy{
				MaximumAttempts: 1,
			},
			WorkflowIDReusePolicy: enums.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE,
			WorkflowID:            fmt.Sprint("ChildWorkflow_V2-withId=[", elements[i], "]-", workflow.GetInfo(ctx).OriginalRunID),
		})
		//_ = workflow.ExecuteChildWorkflow(childWorkflowCtx, ChildWorkflow_V2).GetChildWorkflowExecution().Get(ctx, nil)
		pendingTestFuture := workflow.ExecuteChildWorkflow(childWorkflowCtx, ChildWorkflow_V2).GetChildWorkflowExecution()

		pendingTests.AddFuture(pendingTestFuture, func(f workflow.Future) { _ = f.Get(ctx, nil) })

	}

	remainingWorkflows := childWFPerBatch + 100
	for remainingWorkflows > 0 {
		pendingTests.Select(ctx)
		remainingWorkflows -= 1
	}

	//	for i := 0; i < childWFPerBatch+200; i++ {
	//		pendingTests.Select(ctx)
	//	}

	pendingBatch := len(elements) - childWFPerBatch

	newChildWFPerBatch := childWFPerBatch

	if pendingBatch > 0 {
		if pendingBatch < newChildWFPerBatch {
			newChildWFPerBatch = pendingBatch
		}
		newElements := elements[childWFPerBatch:]
		logger.Info("Parent workflow CAN", "WorkflowExecution.id", workflow.GetInfo(ctx).WorkflowExecution.ID, "OriginalRunID", workflow.GetInfo(ctx).OriginalRunID)
		return workflow.NewContinueAsNewError(ctx, ParentWorkflow_V3, newElements, newChildWFPerBatch)

	}

	return nil
}
