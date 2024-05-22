package io.antmendoza.samples;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class WorkflowClientFactory {


    private static WorkflowClient client;

    public static WorkflowClient get() {

        if (client == null) {
            WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
            client = WorkflowClient.newInstance(service);
        }
        return client;
    }

}
