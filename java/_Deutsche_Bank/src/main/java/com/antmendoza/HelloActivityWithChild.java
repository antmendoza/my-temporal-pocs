package com.antmendoza;

import io.temporal.activity.*;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.ParentClosePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.*;

import java.time.Duration;

public class HelloActivityWithChild {

    static final String TASK_QUEUE = "HelloActivityWithChildTaskQueue";

    static final String WORKFLOW_ID = "HelloActivityWithChildWorkflow";


    public static void main(String[] args) {

        // Get a Workflow service stub.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        /*
         * Get a Workflow service client which can be used to start, Signal, and Query Workflow Executions.
         */
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);


        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());


        factory.start();

        // Create the workflow client stub. It is used to start our workflow execution.
        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                .build());


        String greeting = workflow.getGreeting(new WorkflowInput(true));

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
        String getGreeting(final WorkflowInput workflowInput);
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
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

        @Override
        public String getGreeting(final WorkflowInput workflowInput) {

            //Simulate some work
            Workflow.sleep(Duration.ofSeconds(3));

            if (workflowInput.startChildWorkflowAsync()){


                GreetingWorkflow child = Workflow.newChildWorkflowStub(GreetingWorkflow.class,
                        ChildWorkflowOptions.newBuilder()
                                .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_ABANDON)
                                .build());


                Promise<String> childPromise = Async.function(child::getGreeting, new WorkflowInput(false));
                Promise<WorkflowExecution> childExecution = Workflow.getWorkflowExecution(child);
                // Wait for child to start
                childExecution.get();


                try{
                    childPromise.get();
                }catch (Exception e){

                }

            }

            // This is a blocking call that returns only after the activity has completed.
            return activities.composeGreeting("Hello");
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
