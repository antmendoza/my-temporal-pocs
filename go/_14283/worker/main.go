package main

import (
	"context"
	_14283 "github.com/temporalio/samples-go"
	"log"
	"os"
	"os/signal"
	"syscall"
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

	w := worker.New(c, "greetings-local", worker.Options{
		//WorkerStopTimeout: 20 * time.Second,
	})

	w.RegisterWorkflow(_14283.GreetingSample)
	activities := &_14283.Activities{Name: "Temporal", Greeting: "Hello"}
	w.RegisterActivity(activities)

	stop := make(chan os.Signal, 1)
	signal.Notify(stop, os.Interrupt, syscall.SIGTERM)

	w.Start()
	<-stop
	log.Println("Shutting down worker gracefully...")
	_, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	w.Stop()
	//<-ctx.Done()
}
