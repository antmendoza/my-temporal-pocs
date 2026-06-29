package io.temporal.samples.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import java.util.concurrent.CountDownLatch;

/**
 * Long-lived worker: starts the worker, kicks off the workflow, then blocks so it keeps polling the
 * task queue and can serve queries (including from a client on a different SDK version).
 */
public class WorkerMain {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("WorkerMain SDK version: " + io.temporal.serviceclient.Version.LIBRARY_VERSION);

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactoryOptions factoryOptions =
                WorkerFactoryOptions.newBuilder()
                        .setWorkerInterceptors(new VersionSearchAttributeInterceptor())
                        .build();

        WorkerFactory factory = WorkerFactory.newInstance(client, factoryOptions);
        Worker worker = factory.newWorker(Starter.TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
        factory.start();

        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(Starter.WORKFLOW_ID)
                                .setTaskQueue(Starter.TASK_QUEUE)
                                .build());

        String result = workflow.greet("Temporal");
        System.out.println(result);

        System.out.println("Worker staying alive to serve queries; Ctrl-C to stop.");
        new CountDownLatch(1).await();
    }
}
