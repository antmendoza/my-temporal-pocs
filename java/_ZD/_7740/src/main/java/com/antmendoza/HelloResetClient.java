package com.antmendoza;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.ResetWorkflowExecutionRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.failure.ApplicationFailure;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class HelloResetClient {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";

    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);


        client.getWorkflowServiceStubs().blockingStub().resetWorkflowExecution(ResetWorkflowExecutionRequest
                .newBuilder()
                .setWorkflowExecution(WorkflowExecution.newBuilder()
                        .setWorkflowId(WORKFLOW_ID)
                        //.setRunId("d7400b33-81bd-44ac-b2b4-c6289974cd3a")
                        .build())
                .setReason("reset")
//                .setWorkflowTaskFinishEventId(3) workflowTaskStarted
//                .setWorkflowTaskFinishEventId(4) //workflowTaskCompleted
//                .setWorkflowTaskFinishEventId(24) //workflowTaskFailed
                .setWorkflowTaskFinishEventId(24) //workflowTaskFailed
                        .setNamespace("default")
                .setRequestId(""+Math.random())
                .build());

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
