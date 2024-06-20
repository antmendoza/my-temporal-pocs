package main

import (
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
	"log"
	myapp "myapp"
)

func main() {

	temporalClient, err := client.Dial(client.Options{})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer temporalClient.Close()
	yourWorker := worker.New(temporalClient, "encryption", worker.Options{})
	yourWorker.RegisterWorkflow(myapp.MyWorkflow)

	var activities *myapp.YourActivityObject
	yourWorker.RegisterActivity(activities.YourActivityDefinition)

	err = yourWorker.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start Worker", err)
	}
}
