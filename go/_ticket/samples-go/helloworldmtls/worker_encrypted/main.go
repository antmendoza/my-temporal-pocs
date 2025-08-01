package main

import (
	"github.com/temporalio/samples-go/encryption"
	"log"
	"os"

	"github.com/temporalio/samples-go/helloworldmtls"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
)

func main() {
	// The client and worker are heavyweight objects that should be created once per process.
	clientOptions, err := helloworldmtls.ParseClientOptionFlags(os.Args[1:])
	if err != nil {
		log.Fatalf("Invalid arguments: %v", err)
	}
	c, err := client.Dial(clientOptions)
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, "hello-world-mtls", worker.Options{})

	w.RegisterWorkflow(encryption.Workflow)
	w.RegisterActivity(encryption.Activity)
	w.RegisterActivity(encryption.MyActivity)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
}
