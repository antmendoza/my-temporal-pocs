package com.temporal.demos.temporalspringbootdemo;

import com.temporal.demos.temporalspringbootdemo.workflows.DemoWorkflowImpl;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.time.Duration;

@Configuration
public class InitWorker {


    private final ExpensiveService expensiveService;

    public InitWorker(final ExpensiveService expensiveService) throws SSLException {




        final WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setSystemInfoTimeout(Duration.ofSeconds(8_000))
                .build();


        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(options);

        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker("HelloSampleTaskQueue");

        worker.registerWorkflowImplementationTypes(DemoWorkflowImpl.class);


        expensiveService.performTask();
        worker.registerActivitiesImplementations(new com.temporal.demos.temporalspringbootdemo.workflows.activity.MyActivityImpl());

        factory.start();
        this.expensiveService = expensiveService;
    }
}
