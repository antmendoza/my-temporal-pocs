package main

import (
	"fmt"

	"go.temporal.io/sdk/testsuite"
	"go.temporal.io/sdk/workflow"
)

func chainReadyFutureWorkflow(ctx workflow.Context) (int, error) {
	source, sourceSettable := workflow.NewFuture(ctx)

	//comment this line to reproduce a working example
	sourceSettable.SetValue(42)

	result, resultSettable := workflow.NewFuture(ctx)
	resultSettable.Chain(source)

	//uncomment this line to reproduce a working example
	//sourceSettable.SetValue(42)

	fmt.Printf("[workflow] source.IsReady()=%v  result.IsReady()=%v\n",
		source.IsReady(), result.IsReady())
	fmt.Println("[workflow] calling result.Get() ...")

	var value int
	err := result.Get(ctx, &value)

	fmt.Printf("[workflow] result.Get() returned: value=%d err=%v\n", value, err)
	return value, err
}

func main() {
	suite := &testsuite.WorkflowTestSuite{}
	env := suite.NewTestWorkflowEnvironment()
	env.RegisterWorkflow(chainReadyFutureWorkflow)

	env.ExecuteWorkflow(chainReadyFutureWorkflow)

	var result int
	err := env.GetWorkflowResult(&result)
	if err != nil {
		fmt.Printf("[main] workflow completed with error: err=%v\n", err)
	} else {
		fmt.Printf("[main] workflow completed: result=%d\n", result)
	}
}
