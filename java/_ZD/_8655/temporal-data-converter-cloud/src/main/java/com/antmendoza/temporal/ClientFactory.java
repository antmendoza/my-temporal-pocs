package com.antmendoza.temporal;

import com.antmendoza.temporal.codec.CryptCodec;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.util.Collections;

public class ClientFactory {





    public static  SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();


    private static WorkflowServiceStubs service;


    public  static WorkflowClient getWorkflowClient() {


       // gRPC stubs wrapper that talks to the local docker instance of temporal service.
        service = WorkflowServiceStubs.newServiceStubs(WorkflowServiceStubsOptions.newBuilder()
                .setSslContext(sslContextBuilderProvider.getSslContext())
                .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                .build());


        // client that can be used to start and signal workflows
        WorkflowClient client =
                WorkflowClient.newInstance(
                        service,
                        WorkflowClientOptions.newBuilder()
                               .setNamespace(sslContextBuilderProvider.getNamespace())
                                .setDataConverter(
                                        new CodecDataConverter(
                                                DefaultDataConverter.newDefaultInstance(),
                                                Collections.singletonList(new CryptCodec())))
                                .build());
        return client;
    }

    public static SslContextBuilderProvider getSslContextBuilderProvider() {
        return sslContextBuilderProvider;
    }
}
