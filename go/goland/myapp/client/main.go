package main

import (
	"context"
	"go.temporal.io/sdk/client"
	"log"
	"myapp"
)

func main() {

	temporalClient, err := client.Dial(client.Options{})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer temporalClient.Close()

	ctx := context.Background()

	workflowOptions := client.StartWorkflowOptions{
		ID:        "encryption_workflowID",
		TaskQueue: "encryption",
	}

	we, err := temporalClient.ExecuteWorkflow(
		ctx,
		workflowOptions,
		myapp.MyWorkflow,
		myapp.WorkflowRequest{
			NumIterations: 2,
			Name:          "int",
		},
	)

	var result myapp.WorkflowRequest
	err = we.Get(ctx, &result)
	if err != nil {
		log.Fatalln("Unable get workflow result", err)
	}
	log.Println("Workflow result:", result.NumIterations)

}
