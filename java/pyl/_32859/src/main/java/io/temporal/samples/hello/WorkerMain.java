package io.temporal.samples.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;

/**
 * Runs the workflow to completion on this SDK version, then shuts the worker down and exits. The
 * query is served later by {@link QueryClient}, which spins up its own worker.
 */
public class WorkerMain {

    public static void main(String[] args) {
        System.out.println("WorkerMain SDK version: " + io.temporal.serviceclient.Version.LIBRARY_VERSION);

        WorkflowClient client = TemporalClientFactory.newClient();

        WorkerFactoryOptions factoryOptions =
                WorkerFactoryOptions.newBuilder()
                     //   .setWorkerInterceptors(new VersionSearchAttributeInterceptor())
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

        System.out.println("Workflow finished; shutting worker down." +
                "\n-----------\n");
        factory.shutdown();
        System.exit(0);
    }
}
