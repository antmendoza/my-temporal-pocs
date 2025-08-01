package main

import (
	"context"
	"github.com/pborman/uuid"
	_14283 "github.com/temporalio/samples-go"
	"go.temporal.io/sdk/client"
	"log"
)

func main() {
	// The client is a heavyweight object that should be created once per process.
	c, err := client.Dial(client.Options{
		HostPort: client.DefaultHostPort,
	})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	workflowOptions := client.StartWorkflowOptions{
		ID:        "greetings_" + uuid.New(),
		TaskQueue: "greetings-local",
		//		WorkflowTaskTimeout: 20 * time.Second,
	}

	we, err := c.ExecuteWorkflow(context.Background(), workflowOptions, _14283.GreetingSample)
	if err != nil {
		log.Fatalln("Unable to execute workflow", err)
	}
	log.Println("Started workflow", "WorkflowID", we.GetID(), "RunID", we.GetRunID())
}
