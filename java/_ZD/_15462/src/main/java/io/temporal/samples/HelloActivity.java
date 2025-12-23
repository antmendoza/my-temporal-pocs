package io.temporal.samples;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.*;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.temporal.workflow.unsafe.WorkflowUnsafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloActivity {

    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    static final String WORKFLOW_ID = "HelloActivityWorkflow";


    @WorkflowInterface
    public interface GreetingWorkflow {


        @WorkflowMethod
        String getGreeting(String name);

        @QueryMethod
        String myQuery();

    }

    @ActivityInterface
    public interface GreetingActivities {

        @ActivityMethod(name = "greet")
        String composeGreeting(String greeting, String name);
    }

    public static class GreetingWorkflowImpl implements GreetingWorkflow {


        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(20)).build());

        @Override
        public String getGreeting(String name) {

            if (WorkflowUnsafe.isReplaying()) {
                System.out.println("workflow replaying");
            }


            WorkflowUnsafe.deadlockDetectorOff(() -> {


                if (!WorkflowUnsafe.isReplaying()) {
                    System.out.println("Deadlock detected");
                    try {
                        Thread.sleep(3_000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Before responding with workflow task completed");
                }
            });



            Workflow.sleep(Duration.ofSeconds(10));


            return "hello";
        }

        @Override
        public String myQuery() {
            return "";
        }
    }

    public static class GreetingActivitiesImpl implements GreetingActivities {
        private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

        @Override
        public String composeGreeting(String greeting, String name) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Composing greeting...");
            return greeting + " " + name + "!";
        }
    }


    public static void main(String[] args) throws InterruptedException {

        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(
                io.temporal.serviceclient.WorkflowServiceStubsOptions.newBuilder()
                        .setGrpcClientInterceptors(java.util.Collections.singletonList(
                                new LoggingInterceptor()))
                        .build());

        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory_1 = WorkerFactory.newInstance(client);
        WorkerFactory factory_2 = WorkerFactory.newInstance(client);


        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING)
                                .build());


        WorkflowClient.start(workflow::getGreeting, "World");


        Worker worker_1 = factory_1.newWorker(TASK_QUEUE, WorkerOptions.newBuilder().setIdentity("worker1").build());
        worker_1.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
        worker_1.registerActivitiesImplementations(new GreetingActivitiesImpl());
        factory_1.start();


        //to ensure worker 1 get the first workflow task
        Thread.sleep(1_000);


        CompletableFuture.runAsync(() -> {

            Worker worker_2 = factory_2.newWorker(TASK_QUEUE, WorkerOptions.newBuilder().setIdentity("worker2").build());
            worker_2.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
            worker_2.registerActivitiesImplementations(new GreetingActivitiesImpl());
            factory_2.start();
            System.out.println("Worker 2 started at " + new Date());

            System.out.println("Worker 1 shutdown at " + new Date());
            factory_1.shutdown();
            System.out.println("Worker 1 awaitTermination at " + new Date());
            factory_1.awaitTermination(3, TimeUnit.SECONDS);
            System.out.println("Worker 1 AFTER awaitTermination at " + new Date());

        });


        //to ensure worker 1 is not running when query is sent
        Thread.sleep(4_000);


        System.out.println("time before query  " + new Date());
        workflow.myQuery();
        System.out.println("time after query " + new Date());


        Thread.sleep(20_000);

    }
}
