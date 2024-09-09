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
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class HelloReset {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";

    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);


        // Create the workflow client stub. It is used to start our workflow execution.
        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                .build());



        WorkflowClient.start(workflow::getGreeting, "");


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        client.newUntypedWorkflowStub(WORKFLOW_ID).cancel();

    }


    @WorkflowInterface
    public interface GreetingWorkflow {


        @WorkflowMethod
        String getGreeting(String name);
    }

    @ActivityInterface
    public interface GreetingActivities {


        @ActivityMethod
        String composeGreetingFail(boolean fail);
    }

    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowImpl implements GreetingWorkflow {


        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

        @Override
        public String getGreeting(String name) {
            // This is a blocking call that returns only after the activity has completed.
            final String s = activities.composeGreetingFail(false);

            try {

                activities.composeGreetingFail(true);
            } catch (Exception e) {

            }

            Workflow.sleep(5_000);

            activities.composeGreetingFail(true);


            return s;
        }
    }

    /**
     * Simple activity implementation, that concatenates two strings.
     */
    static class GreetingActivitiesImpl implements GreetingActivities {
        private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);


        @Override
        public String composeGreetingFail(final boolean fail) {

            if (fail) {
                throw ApplicationFailure.newNonRetryableFailure("", "");
            }
            return null;

        }
    }
}
