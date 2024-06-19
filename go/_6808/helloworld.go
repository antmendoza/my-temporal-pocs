package _6808

import (
	"context"
	"fmt"
	"math/rand"
	"time"

	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/workflow"

	// TODO(cretz): Remove when tagged
	_ "go.temporal.io/sdk/contrib/tools/workflowcheck/determinism"
)

func Workflow(ctx workflow.Context, name string) (string, error) {
	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
	}
	ctx = workflow.WithActivityOptions(ctx, ao)

	logger := workflow.GetLogger(ctx)
	logger.Info("HelloWorld workflow started", "name", name)

	queryResult := 0
	err := workflow.SetQueryHandler(ctx, "state", func(input []byte) (string, error) {
		sprintf := fmt.Sprintf("num of iterations %d", queryResult)
		fmt.Println("Executing query method with result  " + sprintf)

		return sprintf, nil
	})
	if err != nil {
		logger.Info("SetQueryHandler failed: " + err.Error())
		return "", err
	}

	var result string

	attributes := map[string]interface{}{
		"CustomIntField":     2,
		"CustomKeywordField": "Update1",
	}

	for i := 0; i < 20; i++ {
		queryResult++

		_ = workflow.SideEffect(ctx, func(ctx workflow.Context) interface{} {
			return rand.Intn(1000)
		})

		workflow.UpsertSearchAttributes(ctx, attributes)

		_ = workflow.ExecuteActivity(ctx, Activity1, name).Get(ctx, &result)

		_ = workflow.SideEffect(ctx, func(ctx workflow.Context) interface{} {
			return rand.Intn(1000)
		})

		_ = workflow.ExecuteActivity(ctx, Activity2, name).Get(ctx, &result)

		_ = workflow.SideEffect(ctx, func(ctx workflow.Context) interface{} {
			return rand.Intn(1000)
		})

		_ = workflow.ExecuteActivity(ctx, Activity3, name).Get(ctx, &result)

		_ = workflow.SideEffect(ctx, func(ctx workflow.Context) interface{} {
			return rand.Intn(1000)
		})

		_ = workflow.ExecuteActivity(ctx, Activity4, name).Get(ctx, &result)

		_ = workflow.SideEffect(ctx, func(ctx workflow.Context) interface{} {
			return rand.Intn(1000)
		})

		_ = workflow.ExecuteActivity(ctx, Activity5, name).Get(ctx, &result)

		_ = workflow.ExecuteActivity(ctx, Activity6, name).Get(ctx, &result)

		logger.Info("HelloWorld workflow completed.", "result", result)
	}

	return "result", nil
}

func Activity1(ctx context.Context, name string) (string, error) {

	logger := activity.GetLogger(ctx)
	logger.Info("Activity", "name", name)
	return "Hello " + name + "!", nil
}

func Activity2(ctx context.Context, name string) (string, error) {

	logger := activity.GetLogger(ctx)
	logger.Info("Activity", "name", name)
	return "Hello " + name + "!", nil
}

func Activity3(ctx context.Context, name string) (string, error) {

	logger := activity.GetLogger(ctx)
	logger.Info("Activity", "name", name)
	return "Hello " + name + "!", nil
}

func Activity4(ctx context.Context, name string) (string, error) {

	logger := activity.GetLogger(ctx)
	logger.Info("Activity", "name", name)
	return "Hello " + name + "!", nil
}

func Activity5(ctx context.Context, name string) (string, error) {

	logger := activity.GetLogger(ctx)
	logger.Info("Activity", "name", name)
	return "Hello " + name + "!", nil
}

func Activity6(ctx context.Context, name string) (string, error) {

	logger := activity.GetLogger(ctx)
	logger.Info("Activity", "name", name)
	return "Hello " + name + "!", nil
}
