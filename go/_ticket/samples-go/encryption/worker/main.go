package main

import (
	"github.com/temporalio/samples-go/helloworldmtls"
	"log"

	"github.com/temporalio/samples-go/encryption"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/converter"
	"go.temporal.io/sdk/worker"
	"go.temporal.io/sdk/workflow"
)

func main() {
	// The client and worker are heavyweight objects that should be created once per process.
	c, err := client.Dial(client.Options{
		// If you intend to let the dataConverter to decide encryption key for all workflows
		// you can set the KeyID for the encryption encoder like so:
		//
		//   DataConverter: encryption.NewEncryptionDataConverter(
		// 	  converter.GetDefaultDataConverter(),
		// 	  encryption.DataConverterOptions{KeyID: "test", Compress: true},
		//   ),
		//
		// In this case you do not need to use a ContextPropagator.
		// You also can implement the dataConverter to decide the encryption key
		// dynamically so that it's not always the same key.
		//
		// If you need to let the workflow starter to decide the encryption key per workflow,
		// you can instead leave the KeyID unset for the encoder and supply it via the workflow
		// context as shown below. For this use case you will also need to use a
		// ContextPropagator so that KeyID is also available in the context for activities.
		//
		// Set DataConverter to ensure that workflow inputs and results are
		// encrypted/decrypted as required.
		DataConverter: encryption.NewEncryptionDataConverter(
			converter.GetDefaultDataConverter(),
			encryption.DataConverterOptions{Compress: true},
		),
		// Use a ContextPropagator so that the KeyID value set in the workflow context is
		// also availble in the context for activities.
		ContextPropagators: []workflow.ContextPropagator{encryption.NewContextPropagator()},
	})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, "hello-world-mtls", worker.Options{})

	w.RegisterWorkflow(helloworldmtls.Workflow)
	w.RegisterActivity(helloworldmtls.Activity)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
}
