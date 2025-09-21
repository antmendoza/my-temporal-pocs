package main

import (
	"context"
	"log"
	"os"

	codecserver "github.com/temporalio/samples-go/codec-server"
	"go.temporal.io/sdk/client"
)

func main() {

	clientOptions, err := codecserver.ParseClientOptionFlags(os.Args[1:])
	if err != nil {
		log.Fatalf("Invalid arguments: %v", err)
	}
	c, err := client.Dial(clientOptions)

	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	workflowOptions := client.StartWorkflowOptions{
		ID:        "codecserver_workflowID",
		TaskQueue: "codecserver",
	}

	// Build protobuf input with two fields: input1, input2
	payload := &codecserver.Input{Input1: "foo", Input2: "bar"}

	we, err := c.ExecuteWorkflow(
		context.Background(),
		workflowOptions,
		codecserver.Workflow,
		payload,
	)
	if err != nil {
		log.Fatalln("Unable to execute workflow", err)
	}

	log.Println("Started workflow", "WorkflowID", we.GetID(), "RunID", we.GetRunID())

	// Synchronously wait for the workflow completion.
	var result string
	err = we.Get(context.Background(), &result)
	if err != nil {
		log.Fatalln("Unable get workflow result", err)
	}
	log.Println("Workflow result:", result)
}
