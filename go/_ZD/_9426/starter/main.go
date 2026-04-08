package main

import (
	helloworld "_9426"
	"context"
	"fmt"
	"log"
	"math/rand"
	"os"

	"go.temporal.io/sdk/client"
)

func main() {
	clientOptions, err := helloworld.ParseClientOptionFlags(os.Args[1:])
	if err != nil {
		log.Fatalf("Invalid arguments: %v", err)
	}
	c, err := client.Dial(clientOptions)
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
