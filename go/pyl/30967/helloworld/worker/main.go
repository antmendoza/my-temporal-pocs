package main

import (
	"github.com/temporalio/samples-go/helloworld/activities_v1"
	"github.com/temporalio/samples-go/helloworld/activities_v2"
	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/contrib/envconfig"
	"go.temporal.io/sdk/worker"

	"github.com/temporalio/samples-go/helloworld"
)

func main() {
	c, _ := client.Dial(envconfig.MustLoadDefaultClientOptions())
	defer c.Close()

	w := worker.New(c, "hello-world", worker.Options{})

	w.RegisterWorkflow(helloworld.Workflow)

	w.RegisterActivity(activities_v1.Activity)
	w.RegisterActivityWithOptions(
		activities_v2.Activity,
		activity.RegisterOptions{
			Name: "Activity_v2",
		},
	)
	_ = w.Run(worker.InterruptCh())

}
