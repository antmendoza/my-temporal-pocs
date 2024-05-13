package main

import (
	helloworld "github.com/temporalio/samples-go"
	"log"
	"time"

	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
)

func main() {
	// The client and worker are heavyweight objects that should be created once per process.
	c, err := client.Dial(client.Options{})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, "hello-world", worker.Options{
		WorkerStopTimeout: 10,
	})

	w.RegisterWorkflow(helloworld.Workflow)
	w.RegisterActivity(helloworld.Activity)

	err = w.Start()
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
	time.Sleep(6 * time.Second)
	//defer
	w.Stop()

}
