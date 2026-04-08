package main

import (
	"context"
	"github.com/google/uuid"
	"go.temporal.io/sdk/client"
	"google.golang.org/grpc"
	"helloworld"
	"log"
	"time"
)

func main() {

	// The client is a heavyweight object that should be created once per process.
	_ = grpc.WithUnaryInterceptor(func(
		ctx context.Context,
		method string,
		req interface{},
		reply interface{},
		cc *grpc.ClientConn,
		invoker grpc.UnaryInvoker,
		opts ...grpc.CallOption,
	) error {
		time.Sleep(1 * time.Second)
		log.Printf("try again in 1 second ")

		err := invoker(ctx, method, req, reply, cc, opts...)
		if err != nil {

			log.Printf("error try again in 1 second %s", method)
			time.Sleep(10 * time.Second)
			log.Printf("try again in 1 second ")

		}

		return invoker(ctx, method, req, reply, cc, opts...)
	})

	c, err := client.NewLazyClient(client.Options{
		ConnectionOptions: client.ConnectionOptions{
			DialOptions: []grpc.DialOption{
				//grpc.WithUnaryInterceptor(grpc_retry.UnaryClientInterceptor(opts...)),
			},
		},
	})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	log.Printf("timer")
	//time.Sleep(10 * time.Second)
	log.Printf("after timer ")

	for i := 0; i < 20; i++ {
		workflowOptions := client.StartWorkflowOptions{
			ID:        "hello_world_workflowID" + uuid.NewString(),
			TaskQueue: "hello-world",
		}

		we, err := c.ExecuteWorkflow(context.Background(), workflowOptions, helloworld.Workflow, "Temporal")
		if err != nil {
			log.Fatalln("Unable to execute workflow", err)
		}

		log.Println("Started workflow", "WorkflowID", we.GetID(), "RunID", we.GetRunID())

		log.Printf("timer-2")
		time.Sleep(5 * time.Second)
		log.Printf("after timer-2")

		err_ := c.SignalWorkflow(context.Background(), we.GetID(), we.GetRunID(), "signalName", nil)
		if err_ != nil {
			log.Fatalln("Unable to signal workflow", err_)
		}

		log.Printf("workflow signaled")

		//// Synchronously wait for the workflow completion.
		//var result string
		//err = we.Get(context.Background(), &result)
		//if err != nil {
		//	log.Fatalln("Unable get workflow result", err)
		//}
		//log.Println("Workflow result:", result)

	}

}
