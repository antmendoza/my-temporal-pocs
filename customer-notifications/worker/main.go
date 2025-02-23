package main

import (
	"customer"
	"log"
	"time"

	"go.temporal.io/sdk/worker"
)

func main() {
	c, err := customer.GetClient()

	defer c.Close()

	w := worker.New(c, "hello-world",
		customer.GetWorkerOptions(),
	)

	w.RegisterWorkflow(customer.NotifyCell)
	w.RegisterWorkflow(customer.NotifyCustomers)

	activities := &customer.NotificationActivity{WorkflowClient: c}
	w.RegisterActivity(activities)

	err = w.Start()
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
	time.Sleep(60 * time.Minute)
	//defer
	//	w.Stop()

}
