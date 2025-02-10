package io.temporal.samples.hello;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.common.WorkflowExecutionHistory;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.testing.WorkflowReplayer;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class HelloActivityV4_only_list_stringTest {

    public static void main(String[] args) throws InterruptedException {

        // Get a Workflow service stub.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker("TASK_QUEUE");

        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
        factory.start();


        // Create our parent workflow client stub. It is used to start the parent workflow execution.
        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId("WORKFLOW_ID")
                                .setTaskQueue("TASK_QUEUE")
                                .build());


        CompletableFuture.runAsync(() -> {
            // Execute our parent workflow and wait for it to complete.
            String greeting = workflow.getGreeting(null);

            // Display the parent workflow execution results
            System.out.println(greeting);

        });


        Thread.sleep(1000);
        workflow.signal(null);


        Thread.sleep(10_000);

    }
    @Rule
    public TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder().setDoNotStart(true).build();


    @WorkflowInterface
    public interface GreetingWorkflow {


        @WorkflowMethod
        String getGreeting(List<String> names);


        @SignalMethod
        void signal(List<String> names);


    }


    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowImpl implements GreetingWorkflow {


        private boolean signaled = false;

        @Override
        public String getGreeting(List<String> names) {
            // This is a blocking call that returns only after the activity has completed.

            Workflow.await(() -> {
                return this.signaled;
            });

            return "done";
        }

        @Override
        public void signal(List<String> names) {
            System.out.println("Signal received");
            this.signaled = true;
        }
    }




    @Test
    public void HelloActivityV4_only_list_objectTest() throws Exception {

        final String eventHistory = executeWorkflow(HelloActivityV4_only_list_stringTest.GreetingWorkflowImpl.class);

        //Replay with V2
        WorkflowReplayer.replayWorkflowExecution(
                eventHistory, HelloActivityV4_only_list_stringTest.GreetingWorkflowImpl.class);
    }


    private String executeWorkflow(Class<?> workflowImplementationType) throws InterruptedException {

        testWorkflowRule
                .getWorker();

        testWorkflowRule.getWorker().registerWorkflowImplementationTypes(workflowImplementationType);

        testWorkflowRule.getTestEnvironment().start();

        GreetingWorkflow workflow =
                testWorkflowRule
                        .getWorkflowClient()
                        .newWorkflowStub(
                                GreetingWorkflow.class,
                                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());



        WorkflowExecution execution = WorkflowStub.fromTyped(workflow).start("Hello");


        Thread.sleep(500);
        WorkflowStub.fromTyped(workflow).signal("signal", null);

        Thread.sleep(500);
        // wait until workflow completes
        WorkflowStub.fromTyped(workflow).getResult(String.class);

        return new WorkflowExecutionHistory(testWorkflowRule.getHistory(execution)).toJson(true);
    }
}
