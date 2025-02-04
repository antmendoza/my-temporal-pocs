package main

import (
	"context"
	"ctxpropagation"
	"log"
	"time"

	"github.com/pborman/uuid"
	"go.temporal.io/sdk/client"
	opentracing "go.temporal.io/sdk/contrib/opentracing"
	"go.temporal.io/sdk/interceptor"
)

func main() {
	// Set tracer which will be returned by opentracing.GlobalTracer().
	closer, _ := ctxpropagation.SetJaegerGlobalTracer()
	defer func() { _ = closer.Close() }()

	// Create interceptor
	tracingInterceptor, err := opentracing.NewInterceptor(opentracing.TracerOptions{})
	if err != nil {
		log.Fatalf("Failed creating interceptor: %v", err)
	}

	// The client is a heavyweight object that should be created once per process.
	c, err := client.Dial(client.Options{
		HostPort:     client.DefaultHostPort,
		Interceptors: []interceptor.ClientInterceptor{tracingInterceptor},
		//		ContextPropagators: []workflow.ContextPropagator{ctxpropagation.NewContextPropagator()},
	})
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	workflowID := "ctx-propagation_" + uuid.New()
	workflowOptions := client.StartWorkflowOptions{
		ID:        workflowID,
		TaskQueue: "go-taskqueue",
	}

	ctx := context.Background()
	ctx = context.WithValue(ctx, ctxpropagation.PropagateKey, &ctxpropagation.Values{Key: "test", Value: "tested"})

	we, err := c.ExecuteWorkflow(ctx, workflowOptions, ctxpropagation.CtxPropWorkflow)
	if err != nil {
		log.Fatalln("Unable to execute workflow", err)
	}
	log.Println("Started workflow", "WorkflowID", we.GetID(), "RunID", we.GetRunID())

	time.Sleep(time.Second * 10)
}
