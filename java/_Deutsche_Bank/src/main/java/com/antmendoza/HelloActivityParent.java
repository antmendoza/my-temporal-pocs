package com.antmendoza;

import io.temporal.activity.*;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.ChildWorkflowFailure;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HelloActivityParent {

    static final String TASK_QUEUE = "HelloActivityWithChildTaskQueue";

    static final String WORKFLOW_ID = "HelloActivityWithChildWorkflow";


    public static void main(String[] args) {

        // Get a Workflow service stub.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        /*
         * Get a Workflow service client which can be used to start, Signal, and Query Workflow Executions.
         */
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client,
                WorkerFactoryOptions.newBuilder()
                        .build());

        Worker worker = factory.newWorker(TASK_QUEUE,
                WorkerOptions.newBuilder()
                        .build());


        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class,
                HelloActivityChild.GreetingWorkflowChildImpl.class);

        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());


        factory.start();

        // Create the workflow client stub. It is used to start our workflow execution.
        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
//                                .setRetryOptions(RetryOptions.newBuilder()
//                                        .setDoNotRetry(NullPointerException.class.getName()).build())
                                .build());


        String greeting = workflow.start(new WorkflowInput(true, false));

        // Display workflow execution results
        System.out.println(greeting);
        System.exit(0);
    }


    @WorkflowInterface
    public interface GreetingWorkflow {

        /**
         * This is the method that is executed when the Workflow Execution is started. The Workflow
         * Execution completes when this method finishes execution.
         */
        @WorkflowMethod
        String start(final WorkflowInput workflowInput);
    }

    @ActivityInterface
    public interface GreetingActivities {

        @ActivityMethod(name = "greet")
        String composeGreeting(String greeting);
    }

    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowImpl implements GreetingWorkflow {

        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder()
                                .setStartToCloseTimeout(Duration.ofSeconds(2)).build());


        private final List<Promise<AsyncChildResult>> promises = new ArrayList();

        @Override
        public String start(final WorkflowInput workflowInput) {

            //Simulate some work
            Workflow.sleep(Duration.ofSeconds(3));

            promises.add(new AsyncChild("1").start());
            promises.add(new AsyncChild("2").start());

            // This is a blocking call that returns only after the activity has completed.
            final String hello = activities.composeGreeting("Hello");

            try {
                Promise.allOf(promises);
            } finally {
                for (Promise<AsyncChildResult> promise : promises) {
                    System.out.println(promise.get());
                }
            }

            return hello;
        }
    }

    /**
     * Simple activity implementation, that concatenates two strings.
     */
    static class GreetingActivitiesImpl implements GreetingActivities {


        public GreetingActivitiesImpl() {
        }


        @Override
        public String composeGreeting(String greeting) {
            return greeting;
        }

    }
}
