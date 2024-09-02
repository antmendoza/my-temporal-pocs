package com.antmendoza;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.Duration;

public class HelloActivityChild {

    @WorkflowInterface
    public interface GreetingWorkflowChild {

        /**
         * This is the method that is executed when the Workflow Execution is started. The Workflow
         * Execution completes when this method finishes execution.
         */
        @WorkflowMethod
        String start(final WorkflowInput workflowInput);
    }



    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowChildImpl implements GreetingWorkflowChild {



        @Override
        public String start(final WorkflowInput workflowInput) {

            //Simulate some work
            Workflow.sleep(Duration.ofSeconds(3));


            if (workflowInput.throwException()) {
                // throw ApplicationFailure.newFailure("Error from workflow", "MyError");
                // throw new NullPointerException("my exception");
            }

            return "hello";
        }
    }


}
