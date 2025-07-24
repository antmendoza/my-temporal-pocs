package main

import (
	_14283 "github.com/temporalio/samples-go"
	"log"
	"time"

	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
)

func main() {
	// The client and worker are heavyweight objects that should be created once per process.
	c, err := client.Dial(client.Options{
		HostPort: client.DefaultHostPort,
	})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, "greetings-local", worker.Options{})

	w.RegisterWorkflow(_14283.GreetingSample)
	activities := &_14283.Activities{Name: "Temporal", Greeting: "Hello"}
	w.RegisterActivity(activities)

	w.Start()
	time.Sleep(4 * time.Second)

	w.Stop()
	
}
