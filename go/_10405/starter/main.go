package main

import (
	helloworld "_10405"
	"context"
	"fmt"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
	"log"
	"math/rand"
)

func main() {

	c, err := helloworld.GetClient()

	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	if false {
		w := worker.New(c, "hello-world",
			helloworld.GetWorkerOptions(),
		)

		w.RegisterWorkflow(helloworld.SendPlay)

		activities := &helloworld.MyActivity{WorkflowClient: c}

		w.RegisterActivity(activities)

		err = w.Start()
		if err != nil {
			log.Fatalln("Unable to start worker", err)
		}
	}

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
