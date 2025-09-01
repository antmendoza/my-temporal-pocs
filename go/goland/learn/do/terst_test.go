package do

import (
	"fmt"
	"github.com/stretchr/testify/require"
	"go.temporal.io/sdk/testsuite"
	"go.temporal.io/sdk/workflow"
	"testing"
)

func TestParentChildSignalExchange(t *testing.T) {
	var ts testsuite.WorkflowTestSuite
	env := ts.NewTestWorkflowEnvironment()
	var childReceivedC bool

	SignalB := "signal-B"
	SignalA := "signal-A"
	SignalC := "signal-C"

	mockChildWorkflow := func(ctx workflow.Context, parentWfId string) error {
		s := workflow.NewSelector(ctx)
		s.AddReceive(workflow.GetSignalChannel(ctx, SignalA), func(c workflow.ReceiveChannel, _ bool) {
			var val string
			c.Receive(ctx, &val)
			fmt.Println("Child received signal A")
			_ = workflow.SignalExternalWorkflow(ctx, parentWfId, "", SignalB, "payload-B").Get(ctx, nil)
			fmt.Println("Child sent signal B")
		})

		s.AddReceive(workflow.GetSignalChannel(ctx, SignalC), func(c workflow.ReceiveChannel, _ bool) {
			var val string
			c.Receive(ctx, &val)
			fmt.Println("Child received signal C")
			childReceivedC = true
		})

		// Selects for signal A and signal C
		s.Select(ctx)
		s.Select(ctx)
		return nil
	}
	parentWorkflow := func(ctx workflow.Context) error {
		cwo := workflow.ChildWorkflowOptions{
			WorkflowID: "child-workflow-id",
		}
		var childWE workflow.Execution
		workflowId := workflow.GetInfo(ctx).WorkflowExecution.ID
		ctx = workflow.WithChildOptions(ctx, cwo)

		// ADDED Get the child workflow execution handle
		childWorkflowFuture := workflow.ExecuteChildWorkflow(ctx, "MockChildWorkflow", workflowId)

		// ADDED Get the child workflow execution details
		err := childWorkflowFuture.GetChildWorkflowExecution().Get(ctx, &childWE)
		if err != nil {
			return err
		}

		workflow.SignalExternalWorkflow(ctx, childWE.ID, childWE.RunID, SignalA, "payload-A")
		fmt.Println("Parent sent signal A")
		var bVal string
		s := workflow.NewSelector(ctx)
		s.AddReceive(workflow.GetSignalChannel(ctx, SignalB), func(c workflow.ReceiveChannel, _ bool) {
			c.Receive(ctx, &bVal)
			fmt.Println("Parent received signal B")
			_ = workflow.SignalExternalWorkflow(ctx, childWE.ID, childWE.RunID, SignalC, "payload-C").Get(ctx, nil)
			fmt.Println("Parent sent signal C")
		})
		s.Select(ctx)

		// ADDED Wait for the child workflow to complete
		var childResult interface{}
		err = childWorkflowFuture.Get(ctx, &childResult)
		if err != nil {
			return err
		}

		return nil
	}
	env.RegisterWorkflow(parentWorkflow)
	env.RegisterWorkflowWithOptions(mockChildWorkflow, workflow.RegisterOptions{Name: "MockChildWorkflow"})

	env.ExecuteWorkflow(parentWorkflow)
	require.True(t, env.IsWorkflowCompleted())
	require.True(t, childReceivedC, "child workflow should have received signal C")
}
