package io.temporal.samples.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;

/**
 * Starts its own worker (on this SDK version) and then queries the workflow. Because a query is
 * answered by replaying the workflow history on a polling worker, running this on a different SDK
 * version than {@link WorkerMain} exercises cross-version replay through the query path.
 */
public class QueryClient {

    public static void main(String[] args) {
        System.out.println("QueryClient SDK version: " + io.temporal.serviceclient.Version.LIBRARY_VERSION);

        WorkflowClient client = TemporalClientFactory.newClient();

        WorkerFactoryOptions factoryOptions =
                WorkerFactoryOptions.newBuilder()
//                        .setWorkerInterceptors(new VersionSearchAttributeInterceptor())
                        .build();

        WorkerFactory factory = WorkerFactory.newInstance(client, factoryOptions);
        Worker worker = factory.newWorker(Starter.TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
        factory.start();

        WorkflowStub stub = client.newUntypedWorkflowStub(Starter.WORKFLOW_ID);
        System.out.println("Querying workflowId=" + Starter.WORKFLOW_ID);

        String result = stub.query("getGreetingQuery", String.class, "Temporal");
        System.out.println("Query result: " + result);
        System.out.println("\n-----------\n");

        factory.shutdown();
        System.exit(0);
    }
}