package io.temporal.samples.hello.new_;

import io.temporal.activity.ActivityOptions;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.samples.hello.*;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.Duration;


public class HelloDynamicActivityMain {

    //public static final DefaultDataConverter dataConverter = new DefaultDataConverter().withPayloadConverterOverrides(new GsonJsonPayloadConverter());

    public static final DefaultDataConverter dataConverter = new DefaultDataConverter().withPayloadConverterOverrides(new MyPayloadConverter());
    static final String TASK_QUEUE = "HelloActivityTaskQueue";
    static final String WORKFLOW_ID = "HelloActivityWorkflow";

    public static void main(String[] args) {

        // Get a Workflow service stub.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();


        WorkflowClient client = WorkflowClient.newInstance(
                service,
                WorkflowClientOptions.newBuilder()
                        .setDataConverter(dataConverter)
                        .build());

        final WorkerFactory factory = WorkerFactory.newInstance(client);


        final Worker worker = factory.newWorker(TASK_QUEUE,
                WorkerOptions.newBuilder().build());


        worker.registerWorkflowImplementationTypes(HelloDynamicActivityTest.WorkflowsImpl.class);
        worker.registerActivitiesImplementations(new   MyDynamicActivityComplexType());

        factory.start();

        WorkflowStub workflow =
                client.newUntypedWorkflowStub(
                        MyWorkflow.class.getSimpleName(),
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_TERMINATE_IF_RUNNING)
                                .setTaskQueue(TASK_QUEUE)
                                .build());



        workflow.start("hello");
        String greeting = workflow.getResult(String.class);

        // Display workflow execution results
        System.out.println(greeting);
        System.exit(0);
    }


    @WorkflowInterface
    public interface GreetingWorkflow {


        @WorkflowMethod
        String getGreeting(String name);
    }

    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowImpl implements HelloActivityRunner.GreetingWorkflow {


        private final TestActivity2<MyRequest2, MyRequest2> activities =
                Workflow.newActivityStub(
                        TestActivity2.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

        @Override
        public String getGreeting(String name) {
            // This is a blocking call that returns only after the activity has completed.
            final MyRequest2 myRequest2 = activities.activity1(new MyRequest2(name, "1"));
            return myRequest2.getName();
        }
    }
}

