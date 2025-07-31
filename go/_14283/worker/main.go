package main

import (
	"log"
	"time"

	_14283 "github.com/temporalio/samples-go"

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

	w := worker.New(c, "greetings-local", worker.Options{
		WorkerStopTimeout: 2 * time.Second,
	})

	w.RegisterWorkflow(_14283.GreetingSample)
	activities := &_14283.Activities{Name: "Temporal", Greeting: "Hello"}
	w.RegisterActivity(activities)

	w.Start()

	println("Started worker.")
	time.Sleep(3 * time.Second)
	w.Stop()
	println("Stoped worker.")

	time.Sleep(30 * time.Second)

}
