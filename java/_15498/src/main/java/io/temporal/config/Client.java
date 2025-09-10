package io.temporal.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

public class Client {


    public final SslContextBuilderProvider sslContextBuilderProvider;

    public Client() {

        this.sslContextBuilderProvider = new SslContextBuilderProvider();

    }

    public  WorkflowClient getWorkflowClient() {


        WorkflowServiceStubs service = getWorkflowServiceStubs();


        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setNamespace(sslContextBuilderProvider.properties.getTemporalNamespace())
                        .build();
        return WorkflowClient.newInstance(service, clientOptions);
    }

    public WorkflowServiceStubs getWorkflowServiceStubs() {
        final WorkflowServiceStubsOptions.Builder builder = WorkflowServiceStubsOptions.newBuilder()
//                                .setRpcTimeout(Duration.ofMillis(167))
                .setTarget(sslContextBuilderProvider.properties.getTemporalTargetEndpoint());

        if (sslContextBuilderProvider.getSslContext() != null) {
            builder.setSslContext(sslContextBuilderProvider.getSslContext());
        }

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        builder
                                .build());
        return service;
    }
}
