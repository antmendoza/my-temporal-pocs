package main

import (
	"log"
	"notification"
	"time"

	"go.temporal.io/sdk/worker"
)

func main() {
	c, err := notification.GetClient()

	defer c.Close()

	w := worker.New(c, "hello-world",
		notification.GetWorkerOptions(),
	)

	w.RegisterWorkflow(notification.NotifyCell)
	w.RegisterWorkflow(notification.NotifyCustomers)
	w.RegisterWorkflow(notification.NotifyCustomer)

	activities := &notification.NotificationActivity{WorkflowClient: c}
	w.RegisterActivity(activities)

	err = w.Start()
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
	time.Sleep(60 * time.Minute)
	//defer
	//	w.Stop()

}
