package child_workflow

import (
	enumspb "go.temporal.io/api/enums/v1"
	"go.temporal.io/sdk/workflow"
)

func SampleParentWorkflow(ctx workflow.Context) (string, error) {
	logger := workflow.GetLogger(ctx)

	cwo := workflow.ChildWorkflowOptions{
		WorkflowID:        "ABC-SIMPLE-CHILD-WORKFLOW-ID",
		ParentClosePolicy: enumspb.PARENT_CLOSE_POLICY_REQUEST_CANCEL,
	}
	ctx = workflow.WithChildOptions(ctx, cwo)

	childWorkflow := workflow.ExecuteChildWorkflow(ctx, SampleChildWorkflow, "World")
	// Wait for child to start
	_ = childWorkflow.GetChildWorkflowExecution().Get(ctx, nil)

	result := "completed"
	logger.Info("Parent execution completed.", "Result", result)
	return result, nil
}

// @@@SNIPEND
