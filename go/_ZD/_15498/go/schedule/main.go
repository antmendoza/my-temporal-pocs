package main

import (
	"context"
	codecserver "github.com/temporalio/samples-go/codec-server"
	"go.temporal.io/api/enums/v1"
	"go.temporal.io/sdk/client"
	"log"
	"os"
)

func main() {

	ctx := context.Background()

	clientOptions, err := codecserver.ParseClientOptionFlags(os.Args[1:])
	if err != nil {
		log.Fatalf("Invalid arguments: %v", err)
	}
	c, err := client.Dial(clientOptions)

	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()
	scheduleID := "schedule_"
	workflowID := "schedule_workflow_"

	wfInput := &codecserver.Input{Input1: "foo2", Input2: "bar"}

	log.Println("Deleting schedule", "ScheduleID", scheduleID)
	err = c.ScheduleClient().GetHandle(ctx, scheduleID).Delete(ctx)
	if err != nil {
	}

	scheduleHandle, err := c.ScheduleClient().Create(ctx, client.ScheduleOptions{
		ID:   scheduleID,
		Spec: client.ScheduleSpec{},
		Action: &client.ScheduleWorkflowAction{
			ID:        workflowID,
			Workflow:  codecserver.Workflow,
			TaskQueue: "codecserver",
			Args:      []interface{}{wfInput},
		},
	})
	if err != nil {
		log.Fatalln("Unable to create schedule", err)
	}

	// Manually trigger the schedule once
	log.Println("Manually triggering schedule", "ScheduleID", scheduleHandle.GetID())

	err = scheduleHandle.Trigger(ctx, client.ScheduleTriggerOptions{
		Overlap: enums.SCHEDULE_OVERLAP_POLICY_ALLOW_ALL,
	})
	if err != nil {
		log.Fatalln("Unable to trigger schedule", err)
	}

}
