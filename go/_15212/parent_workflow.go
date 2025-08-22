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

	err_ := workflow.ExecuteChildWorkflow(ctx, SampleChildWorkflow, "World").Get(ctx, nil)
	//	// Wait for child to start
	//	_ = childWorkflow.GetChildWorkflowExecution().Get(ctx, nil)

	if err_ != nil {
		logger.Error("Child workflow failed.", "Error", err_)
		//return "", err_
	}

	result := "completed"
	logger.Info("Parent execution completed.", "Result", result)
	return result, nil
}
