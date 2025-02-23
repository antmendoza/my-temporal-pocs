package main

import (
	"context"
	"customer"
	"fmt"
	"go.temporal.io/sdk/client"
	"log"
	"math/rand"
)

func main() {

	c, err := customer.GetClient()

	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	input := customer.NotifyCellRequest{
		CellId: "1",
		Text:   "Hello World",
		CustomerType: customer.CustomerTypeFilter{
			Types: []string{"mission-critical", "enterprise"},
		},
	}

	workflowOptions := client.StartWorkflowOptions{
		ID:        "notify-cell[" + input.CellId + "]/" + fmt.Sprintf("%d", rand.Int()),
		TaskQueue: "hello-world",
	}
	we, err := c.ExecuteWorkflow(context.Background(), workflowOptions, customer.NotifyCell, input)
	if err != nil {
		log.Fatalln("Unable to execute workflow", err)
	}

	log.Println("Started workflow", "WorkflowID", we.GetID(), "RunID", we.GetRunID())

	// Synchronously wait for the workflow completion.
	var result customer.NotifyCellResponse
	err = we.Get(context.Background(), &result)
	if err != nil {
		log.Fatalln("Unable get workflow result", err)
	}
	log.Println("Workflow result:", result)
}
