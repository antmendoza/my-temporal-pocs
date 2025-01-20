package main

import (
	helloworld "_10405"
	"context"
	"fmt"
	"go.temporal.io/sdk/client"
	"log"
	"math/rand"
)

func main() {

	c, err := helloworld.GetClient()

	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	workflowOptions := client.StartWorkflowOptions{
		ID:        fmt.Sprintf("%d", rand.Int()),
		TaskQueue: "hello-world",
	}

	type s struct {
		Int       int
		String    string
		ByteSlice []byte
	}

	input := helloworld.WorkflowInput{
		NumberOfActivitiesInParent:           20,
		NumberOfExternalWorkflowsPerActivity: 50,
	}

	we, err := c.ExecuteWorkflow(context.Background(), workflowOptions, helloworld.SendPlay, input)
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
