package io.temporal.samples.hello;

import io.temporal.activity.*;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.*;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloActivity {

    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    static final String WORKFLOW_ID = "HelloActivityWorkflow";



    @ActivityInterface
    public interface MyActivities {

        @ActivityMethod
        String sleep_time(int ms);

        @ActivityMethod
        String start_workflow(int ms);
    }



    @WorkflowInterface
    public static interface MyWorkflow {
        @WorkflowMethod
        String run(String name);

        @UpdateMethod
        String update();

        @SignalMethod
        void signal();

        @QueryMethod
        void query();




    }
    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class MyWorkflowImpl implements MyWorkflow {


        private final Logger logger = Workflow.getLogger(MyWorkflowImpl.class.getName());


        private final MyActivities activities = Workflow.newActivityStub(
                MyActivities.class,
                ActivityOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setStartToCloseTimeout(
                                Duration.ofMillis(15_000)
                        )
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setBackoffCoefficient(1)
                                .setInitialInterval(Duration.ofSeconds(5))
                                .build())
                        .build());

        private final MyActivities localActivity = Workflow.newLocalActivityStub(
                MyActivities.class,
                LocalActivityOptions.newBuilder()
                        .setStartToCloseTimeout(
                                //setting to a very large value for demo purpose.
                                Duration.ofMillis(100_000)
                        )
//                        .setDoNotIncludeArgumentsIntoMarker(true)
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setBackoffCoefficient(1)
                                .build())
                        .build());




        private CompletablePromise<String> promise = null;

        public String run(String name) {


            Async.function(() -> {
                localActivity.start_workflow(1_000);
                promise = Workflow.newPromise();
                return promise;
            });


            localActivity.sleep_time(400);
            localActivity.sleep_time(400);


            //artificially sleep the workflow to allow the local activity to complete.
            Workflow.sleep(20_000);



            return "done";

        }

        @Override
        public String update() {
            return null;
        }

        @Override
        public void signal() {
            logger.info("Signal received ...");


            //another approach is setting a variable to true and moving the workflow
            //await to the main workflow method.
            //this.signaled = true;
            //and then in the main workflow method:
            //Workflow.await(() -> promise != null && this.signaled);
            Workflow.await(() -> promise != null);

            logger.info(" promise: " + promise);

            logger.info("Processing signal ...");
            promise.complete(null);


        }

        @Override
        public void query() {
            System.out.println("Querying ...");
        }


    }

    public static class MyActivitiesImpl implements MyActivities {
        private static final Logger log = LoggerFactory.getLogger("-");

        @Override
        public String sleep_time(int ms) {

            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return "";
        }

        @Override
        public String start_workflow(int ms) {

            log.info("start_workflow activity: Starting a new workflow from activity...");

            sleep_time(ms);

            log.info("start_workflow activity: About to complete ...");

            return "";
        }


    }
    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);

        worker.registerActivitiesImplementations(new MyActivitiesImpl());

        factory.start();

        // Create the workflow client stub. It is used to start our workflow execution.
        MyWorkflow workflow =
                client.newWorkflowStub(
                        MyWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                //.setPriority(Priority.newBuilder().setPriorityKey(1).build())
                                .build());


        WorkflowClient.start(workflow::run, "World");


        // wait for a few seconds to let the local activity start and start the external workflow
        try {
            Thread.sleep(2_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // send a signal to the workflow
        workflow.signal();

        // wait for workflow completion
        client.newUntypedWorkflowStub(WORKFLOW_ID).getResult(String.class);


        DescribeWorkflowExecutionResponse describe = client.getWorkflowServiceStubs().blockingStub().describeWorkflowExecution(DescribeWorkflowExecutionRequest.newBuilder()
                        .setNamespace("default")
                        .setExecution(WorkflowExecution.newBuilder().setWorkflowId(WORKFLOW_ID).build())
                .build());



        System.exit(0);
    }
}
