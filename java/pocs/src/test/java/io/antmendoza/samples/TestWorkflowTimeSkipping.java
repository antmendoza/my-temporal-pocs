package io.antmendoza.samples;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import io.temporal.worker.Worker;
import io.temporal.workflow.Async;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.atomic.AtomicBoolean;

class TestWorkflowTimeSkipping {


    @RegisterExtension
    TestWorkflowExtension testWorkflowExtension =
            TestWorkflowExtension.newBuilder()
                    .setDoNotStart(true)
                    .build();


    // Time skipping does not work here,
    // test execution takes 2 seconds,
    // WorkflowAwait only unblock when the activity returns in one second
    @org.junit.jupiter.api.Test
    public void testBlockingActivityInAwait(
            TestWorkflowEnvironment testWorkflowEnvironment,
            WorkflowClient workflowClient,
            Worker worker
    ) {

        // using Workflow_AwaitBlocking as workflow implementation
        worker.registerWorkflowImplementationTypes(Workflow_AwaitBlocking.class);

        worker.registerActivitiesImplementations(new Activity() {
            @Override
            public String doSomething() {
                try {
                    Thread.sleep(2_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });

        testWorkflowEnvironment.start();


        long start = System.currentTimeMillis();

        final Workflow_ workflow = testWorkflowEnvironment.getWorkflowClient().newWorkflowStub(Workflow_.class, WorkflowOptions
                .newBuilder()
                .setTaskQueue(worker.getTaskQueue())
                .build());
        WorkflowExecution execution = WorkflowClient.start(workflow::run);

        //To force await timeout
        testWorkflowEnvironment.sleep(Duration.ofSeconds(5));

        TimerFired result = getWorkflowResult(workflowClient, execution);

        //The timer didn't fire, I would expect it fired since we are doing
        //testWorkflowEnvironment.sleep(Duration.ofSeconds(5));
        Assertions.assertEquals(new TimerFired(false), result);

        long end = System.currentTimeMillis();
        final long timeInSeconds = (end - start) / 1000;
        //This fails, test takes 2 seconds
        Assertions.assertTrue(timeInSeconds < 2, () -> "Test takes: " + timeInSeconds + " seconds");


    }

    // This test never completes, becase the activity in the await method never completes
    // and the timer in workflow.await doesn't fire
    @org.junit.jupiter.api.Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testNeverCompletes(
            TestWorkflowEnvironment testWorkflowEnvironment,
            WorkflowClient workflowClient,
            Worker worker
    ) {


        // using Workflow_AwaitBlocking as workflow implementation
        worker.registerWorkflowImplementationTypes(Workflow_AwaitBlocking.class);

        worker.registerActivitiesImplementations(new Activity() {
            @Override
            public String doSomething() {
                if (true) {
                    throw new RuntimeException("fake exception");
                }
                return null;
            }
        });


        testWorkflowEnvironment.start();

        final Workflow_ workflow = testWorkflowEnvironment.getWorkflowClient().newWorkflowStub(Workflow_.class, WorkflowOptions
                .newBuilder()
                .setTaskQueue(worker.getTaskQueue())
                .build());
        WorkflowExecution execution = WorkflowClient.start(workflow::run);

        //To force await timeout
        testWorkflowEnvironment.sleep(Duration.ofSeconds(5));

        //It does not mather the time move forward the time, workflow.await never unblock
        TimerFired result = getWorkflowResult(workflowClient, execution);


    }

    // Time skipping does not work here,
    // test execution takes 5 seconds, until real timer fires in workflow.await
    @org.junit.jupiter.api.Test
    public void testNoBlockingActivityInAwait(
            TestWorkflowEnvironment testWorkflowEnvironment,
            WorkflowClient workflowClient,
            Worker worker
    ) {


        // using Workflow_AwaitNotBlocking as workflow implementation
        worker.registerWorkflowImplementationTypes(Workflow_AwaitNotBlocking.class);

        worker.registerActivitiesImplementations(new Activity() {
            @Override
            public String doSomething() {
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });

        testWorkflowEnvironment.start();

        long start = System.currentTimeMillis();


        final Workflow_ workflow = testWorkflowEnvironment.getWorkflowClient().newWorkflowStub(Workflow_.class, WorkflowOptions
                .newBuilder()
                .setTaskQueue(worker.getTaskQueue())
                .build());
        WorkflowExecution execution = WorkflowClient.start(workflow::run);

        //To force await timeout
        testWorkflowEnvironment.sleep(Duration.ofSeconds(5));

        TimerFired result = getWorkflowResult(workflowClient, execution);

        //The timer fires, but after 5 real seconds
        Assertions.assertEquals(new TimerFired(true), result);

        long end = System.currentTimeMillis();
        final long timeInSeconds = (end - start) / 1000;
        //This fails, test takes 5 seconds
        Assertions.assertTrue(timeInSeconds <= 2, () -> "Test takes: " + timeInSeconds + " seconds");

    }



    // Time skipping works here, with no blocking activity involved
    @org.junit.jupiter.api.Test
    public void testNoActivityAtAll(
            TestWorkflowEnvironment testWorkflowEnvironment,
            WorkflowClient workflowClient,
            Worker worker
    ) {


        // using Workflow_AwaitNotBlocking as workflow implementation
        worker.registerWorkflowImplementationTypes(Workflow_NotActivity.class);


        testWorkflowEnvironment.start();

        long start = System.currentTimeMillis();


        final Workflow_ workflow = testWorkflowEnvironment.getWorkflowClient().newWorkflowStub(Workflow_.class, WorkflowOptions
                .newBuilder()
                .setTaskQueue(worker.getTaskQueue())
                .build());
        WorkflowExecution execution = WorkflowClient.start(workflow::run);

        //To force await timeout
        testWorkflowEnvironment.sleep(Duration.ofSeconds(5));

        TimerFired result = getWorkflowResult(workflowClient, execution);

        //The timer fires, but after 5 real seconds
        Assertions.assertEquals(new TimerFired(true), result);

        long end = System.currentTimeMillis();
        final long timeInSeconds = (end - start) / 1000;
        Assertions.assertTrue(timeInSeconds <= 2, () -> "Test takes: " + timeInSeconds + " seconds");

    }


    private TimerFired getWorkflowResult(final WorkflowClient workflowClient, final WorkflowExecution execution) {


        String workflowId = execution.getWorkflowId();
        TimerFired result = workflowClient.newUntypedWorkflowStub(workflowId).getResult(TimerFired.class);

        return result;
    }


    @ActivityInterface
    public interface Activity {

        @ActivityMethod
        String doSomething();

    }


    @WorkflowInterface
    public interface Workflow_ {
        @WorkflowMethod
        TimerFired run();
    }

    public static class Workflow_AwaitBlocking implements Workflow_ {

        private final Activity activity = Workflow.newActivityStub(Activity.class,
                ActivityOptions.newBuilder().
                        setStartToCloseTimeout(Duration.ofSeconds(20))
                        .build()
        );


        @Override
        public TimerFired run() {

            boolean activityExecuted = Workflow.await(Duration.ofSeconds(5), () -> {
                System.out.println("In Workflow.await java");
                activity.doSomething();
                return true;
            });


            System.out.println("activityExecuted: " + activityExecuted);

            return new TimerFired(!activityExecuted);
        }

    }

    public static class Workflow_AwaitNotBlocking implements Workflow_ {

        private final Activity activity = Workflow.newActivityStub(Activity.class,
                ActivityOptions.newBuilder().
                        setStartToCloseTimeout(Duration.ofSeconds(20))
                        .build()
        );


        @Override
        public TimerFired run() {

            final AtomicBoolean activityCompleted = new AtomicBoolean(false);

            Async.procedure(activity::doSomething).thenApply((r) -> {
                activityCompleted.set(true);
                return r;
            });

            boolean activityExecuted = Workflow.await(Duration.ofSeconds(5), () -> {
                System.out.println("In Workflow.await java");
                final boolean b = activityCompleted.get();
                System.out.println("In Workflow.await java, completed: " + b);
                return b;
            });


            System.out.println("activityExecuted: " + activityExecuted);


            return new TimerFired(!activityExecuted);
        }

    }



    public static class Workflow_NotActivity implements Workflow_ {



        @Override
        public TimerFired run() {


            boolean activityExecuted = Workflow.await(Duration.ofSeconds(5), () -> {
                return false;
            });


            System.out.println("activityExecuted: " + activityExecuted);


            return new TimerFired(!activityExecuted);
        }

    }

    public record TimerFired(boolean timerFired) {

    }

}