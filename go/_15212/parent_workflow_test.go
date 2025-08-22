package child_workflow_test

import (
	child_workflow "_15212"
	"github.com/stretchr/testify/require"
	"go.temporal.io/sdk/testsuite"
	"testing"
)

func Test_Workflow_Integration(t *testing.T) {
	testSuite := &testsuite.WorkflowTestSuite{}
	env := testSuite.NewTestWorkflowEnvironment()
	env.RegisterWorkflow(child_workflow.SampleChildWorkflow)

	env.ExecuteWorkflow(child_workflow.SampleParentWorkflow)

	err := env.GetWorkflowErrorByID("ABC-SIMPLE-CHILD-WORKFLOW-ID")
	require.Error(t, err)

	require.True(t, env.IsWorkflowCompleted())
	require.NoError(t, env.GetWorkflowError())
	var result string
	require.NoError(t, env.GetWorkflowResult(&result))

	require.Equal(t, "completed", result)

}
