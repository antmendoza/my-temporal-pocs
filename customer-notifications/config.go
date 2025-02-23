package customer

import (
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
)

func GetClient() (client.Client, error) {
	c, err := client.Dial(client.Options{})
	return c, err
}

func GetWorkerOptions() worker.Options {
	return worker.Options{
		MaxConcurrentActivityExecutionSize:     1000,
		MaxConcurrentWorkflowTaskExecutionSize: 1000,
		MaxConcurrentActivityTaskPollers:       100, //we have eager dispatch for activities.
		MaxConcurrentWorkflowTaskPollers:       500,
	}
}
