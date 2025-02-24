package notification

import (
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
)

func GetClient() (client.Client, error) {
	c, err := client.Dial(client.Options{})
	return c, err
}

func GetWorkerOptions() worker.Options {
	return worker.Options{}
}
