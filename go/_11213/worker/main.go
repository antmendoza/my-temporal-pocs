package main

import (
	"_11213"
	"context"
	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/client"
	sdkinterceptor "go.temporal.io/sdk/interceptor"
	"go.temporal.io/sdk/worker"
	"go.temporal.io/sdk/workflow"
	"log"
	"time"
)

func main() {
	// The client and worker are heavyweight objects that should be created once per process.
	c, err := client.Dial(client.Options{})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, "interceptor", worker.Options{
		// Create interceptor that will put started time on the logger
		Interceptors: []sdkinterceptor.WorkerInterceptor{_11213.NewWorkerInterceptor(_11213.InterceptorOptions{
			GetExtraLogTagsForWorkflow: func(ctx workflow.Context) []interface{} {
				return []interface{}{"WorkflowStartTime", workflow.GetInfo(ctx).WorkflowStartTime.Format(time.RFC3339)}
			},
			GetExtraLogTagsForActivity: func(ctx context.Context) []interface{} {
				return []interface{}{"ActivityStartTime", activity.GetInfo(ctx).StartedTime.Format(time.RFC3339)}
			},
		})},
	})

	w.RegisterWorkflow(_11213.Workflow)
	w.RegisterActivity(_11213.Activity)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
}
