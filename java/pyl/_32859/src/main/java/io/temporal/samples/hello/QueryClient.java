package io.temporal.samples.hello;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;

/**
 * Standalone client that only queries an already-running/completed workflow. Run this with a
 * different {@code -Dtemporal.version} than the worker to exercise cross-SDK-version querying.
 */
public class QueryClient {

    public static void main(String[] args) {
        System.out.println("QueryClient SDK version: " + io.temporal.serviceclient.Version.LIBRARY_VERSION);

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkflowStub stub = client.newUntypedWorkflowStub(Starter.WORKFLOW_ID);
        WorkflowExecution exec = stub.getExecution();
        System.out.println("Querying workflowId=" + exec.getWorkflowId());

        String result = stub.query("getGreetingQuery", String.class, "Temporal");
        System.out.println("Query result: " + result);

        System.exit(0);
    }
}