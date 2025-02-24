package main

import (
	"context"
	"fmt"
	"go.temporal.io/sdk/client"
	"log"
	"math/rand"
	"notification"
)

func main() {

	c, err := notification.GetClient()

	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	cellId := "1"
	input := notification.NotifyCellRequest{
		CellId: cellId,
		Text:   "Hello World",
		CustomerType: notification.CustomerTypeFilter{
			CellId: cellId,
			Types:  []string{"mission-critical", "enterprise"},
		},
	}

	workflowOptions := client.StartWorkflowOptions{
		ID:        "notify-cell[" + input.CellId + "]/" + fmt.Sprintf("%d", rand.Int()),
		TaskQueue: "hello-world",
	}
	we, err := c.ExecuteWorkflow(context.Background(), workflowOptions, notification.NotifyCell, input)
	if err != nil {
		log.Fatalln("Unable to execute workflow", err)
	}

	log.Println("Started workflow", "WorkflowID", we.GetID(), "RunID", we.GetRunID())

	// Synchronously wait for the workflow completion.
	var result notification.NotifyCellResponse
	err = we.Get(context.Background(), &result)
	if err != nil {
		log.Fatalln("Unable get workflow result", err)
	}
	log.Println("Workflow result:", result)
}
