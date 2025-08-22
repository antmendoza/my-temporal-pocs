package child_workflow_test

import (
	child_workflow "_15212"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"go.temporal.io/sdk/temporal"
	"go.temporal.io/sdk/testsuite"
	"testing"
)

func Test_Workflow_Mocked(t *testing.T) {
	testSuite := &testsuite.WorkflowTestSuite{}
	env := testSuite.NewTestWorkflowEnvironment()
	env.RegisterWorkflow(child_workflow.SampleChildWorkflow)

	env.OnWorkflow(child_workflow.SampleChildWorkflow, mock.Anything, mock.Anything).Run(func(args mock.Arguments) {
		println("OnWorkflow called")
		env.CancelWorkflow()
	})

	env.ExecuteWorkflow(child_workflow.SampleParentWorkflow)

	require.True(t, env.IsWorkflowCompleted())
	require.NoError(t, env.GetWorkflowError())
	var result string
	require.NoError(t, env.GetWorkflowResult(&result))

	require.Equal(t, "completed", result)

	err := env.GetWorkflowErrorByID("ABC-SIMPLE-CHILD-WORKFLOW-ID")
	require.Error(t, err)

	require.True(t, true, temporal.IsCanceledError(env.GetWorkflowErrorByID("ABC-SIMPLE-CHILD-WORKFLOW-ID")))
}
