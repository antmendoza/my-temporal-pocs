package main

import (
	"github.com/temporalio/samples-go/workflow"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
	"log"
	"os"
)

func main() {
	// The client and worker are heavyweight objects that should be created once per process.
	clientOptions, err := workflow.ParseClientOptionFlags(os.Args[1:])
	if err != nil {
		log.Fatalf("Invalid arguments: %v", err)
	}
	c, err := client.Dial(clientOptions)
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, "hello-parent", worker.Options{
		//		MaxConcurrentActivityExecutionSize:     5,
		MaxConcurrentWorkflowTaskExecutionSize: 1000,
		MaxConcurrentActivityTaskPollers:       6,
		MaxConcurrentWorkflowTaskPollers:       6,
		//		TaskQueueActivitiesPerSecond:           200,
	})

	worker.SetStickyWorkflowCacheSize(100)
	w.RegisterWorkflow(workflow.ParentWorkflow_V3)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
}
