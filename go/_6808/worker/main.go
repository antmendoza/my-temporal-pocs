package main

import (
	"_6808"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
	"log"
)

func main() {
	// The client and worker are heavyweight objects that should be created once per process.
	c, err := client.Dial(client.Options{})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, "hello-world", worker.Options{
		//	WorkerStopTimeout: 10,
	})

	w.RegisterWorkflow(_6808.Workflow)
	w.RegisterActivity(_6808.Activity1)
	w.RegisterActivity(_6808.Activity2)
	w.RegisterActivity(_6808.Activity3)
	w.RegisterActivity(_6808.Activity4)
	w.RegisterActivity(_6808.Activity5)
	w.RegisterActivity(_6808.Activity6)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}

}
