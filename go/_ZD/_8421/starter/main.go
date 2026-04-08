package main

import (
	"context"
	"encoding/json"
	"github.com/pborman/uuid"
	"go.temporal.io/api/enums/v1"
	"go.temporal.io/sdk/client"
	"log"
	"time"
)

func main() {
	ctx := context.Background()
	// The client is a heavyweight object that should be created once per process.
	c, err := client.Dial(client.Options{
		HostPort: client.DefaultHostPort,
		//DataConverter: _8421.MyDataConverter,
	})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	inputStringCar := "{\"FieldA\": \"FieldAValueCar\",\"FieldB\": \"FieldBValueCar\"}"
	var jsonInputCar map[string]interface{}
	json.Unmarshal([]byte(inputStringCar), &jsonInputCar)

	inputStringCar2 := "{\"FieldA\": \"FieldAValueCar_2\",\"FieldB\": \"FieldBValueCar_2\"}"
	var jsonInputCar2 map[string]interface{}
	json.Unmarshal([]byte(inputStringCar2), &jsonInputCar2)

	runSchedule(err, c, ctx, "SampleScheduleWorkflowCar", []any{jsonInputCar, jsonInputCar2})

	inputStringCat := "{\"FieldCatA\": \"FieldAValueCat\",\"FieldCatB\": \"FieldBValueCat\"}"
	var jsonInputCat map[string]interface{}
	json.Unmarshal([]byte(inputStringCat), &jsonInputCat)

	runSchedule(err, c, ctx, "SampleScheduleWorkflowCat", []any{jsonInputCat})

	time.Sleep(2 * time.Second)
}

func runSchedule(
	err error,
	c client.Client,
	ctx context.Context,
	workflowType string,
	Args []interface{}) {
	// This schedule ID can be user business logic identifier as well.
	scheduleID := "schedule_" + uuid.New()
	workflowID := "schedule_workflow_" + uuid.New()

	scheduleHandle, err := c.ScheduleClient().Create(ctx, client.ScheduleOptions{
		ID:   scheduleID,
		Spec: client.ScheduleSpec{},
		Action: &client.ScheduleWorkflowAction{
			ID:        workflowID,
			Workflow:  workflowType,
			TaskQueue: "schedule",
			Args:      Args,
		},
	})

	if err != nil {
		log.Fatalln("Unable to create schedule", err)
	}
	// Delete the schedule once the sample is done
	defer func() {
		log.Println("Deleting schedule", "ScheduleID", scheduleHandle.GetID())
		err = scheduleHandle.Delete(ctx)
		if err != nil {
			log.Fatalln("Unable to delete schedule", err)
		}
	}()

	// Manually trigger the schedule once
	log.Println("Manually triggering schedule", "ScheduleID", scheduleHandle.GetID())

	err = scheduleHandle.Trigger(ctx, client.ScheduleTriggerOptions{
		Overlap: enums.SCHEDULE_OVERLAP_POLICY_ALLOW_ALL,
	})
	if err != nil {
		log.Fatalln("Unable to trigger schedule", err)
	}

	time.Sleep(2 * time.Second)
}
