package main

import (
	helloworld "_9426"
	"log"
	"os"
	"time"

	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
)

func main() {
	// The client and worker are heavyweight objects that should be created once per process.
	clientOptions, err := helloworld.ParseClientOptionFlags(os.Args[1:])
	if err != nil {
		log.Fatalf("Invalid arguments: %v", err)
	}
	c, err := client.Dial(clientOptions)
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, "hello-world",
		helloworld.GetWorkerOptions(),
	)

	w.RegisterWorkflow(helloworld.SendPlay)
	w.RegisterWorkflow(helloworld.SendPlayToSubscriber)
	w.RegisterWorkflow(helloworld.SendPlayToSubscriberChild)

	activities := &helloworld.MyActivity{WorkflowClient: c}

	w.RegisterActivity(activities)

	err = w.Start()
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
	time.Sleep(60 * time.Minute)
	//defer
	//	w.Stop()

}
