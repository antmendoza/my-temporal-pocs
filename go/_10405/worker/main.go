package main

import (
	helloworld "_10405"
	"log"
	"time"

	"go.temporal.io/sdk/worker"
)

func main() {
	c, err := helloworld.GetClient()

	defer c.Close()

	w := worker.New(c, "hello-world",
		helloworld.GetWorkerOptions(),
	)

	w.RegisterWorkflow(helloworld.SendPlay)

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
