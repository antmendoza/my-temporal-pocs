package main

import (
	"_8421"
	"log"

	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
)

func main() {
	// The client and worker are heavyweight objects that should be created once per process.
	c, err := client.Dial(client.Options{
		HostPort: client.DefaultHostPort,
		//DataConverter: _8421.MyDataConverter,
	})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, "schedule", worker.Options{})

	w.RegisterWorkflow(_8421.SampleScheduleWorkflowCar)
	w.RegisterWorkflow(_8421.SampleScheduleWorkflowCat)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
}
