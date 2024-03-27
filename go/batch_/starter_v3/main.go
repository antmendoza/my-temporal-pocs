package main

import (
	"context"
	"github.com/temporalio/samples-go/workflow"
	"go.temporal.io/sdk/client"
	"log"
	"math/rand"
	"os"
	"strconv"
)

func main() {
	// The client is a heavyweight object that should be created once per process.
	clientOptions, err := workflow.ParseClientOptionFlags(os.Args[1:])
	if err != nil {
		log.Fatalf("Invalid arguments: %v", err)
	}
	c, err := client.Dial(clientOptions)
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	for i := 0; i < 2; i++ {
		workflowOptions := client.StartWorkflowOptions{
			ID:        "ParentWorkflow_" + strconv.Itoa(i) + "_" + strconv.Itoa(rand.Intn(10000)),
			TaskQueue: "hello-parent",
		}
		childWFPerBatch := 200

		childWorkflowIDs := workflow.MakeRange(1, 2000)
		we, err := c.ExecuteWorkflow(context.Background(), workflowOptions, workflow.ParentWorkflow_V3,
			childWorkflowIDs, childWFPerBatch)
		if err != nil {
			log.Fatalln("Unable to execute workflow", err)
		}
		log.Println("Started workflow", "WorkflowID", we.GetID(), "RunID", we.GetRunID())

	}

}
