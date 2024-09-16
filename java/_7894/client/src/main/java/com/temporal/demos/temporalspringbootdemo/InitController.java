package com.temporal.demos.temporalspringbootdemo;

import io.grpc.Deadline;
import io.temporal.api.workflowservice.v1.GetSystemInfoRequest;
import io.temporal.api.workflowservice.v1.GetSystemInfoResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.internal.retryer.GrpcRetryer;
import io.temporal.serviceclient.RpcRetryOptions;
import io.temporal.serviceclient.SystemInfoInterceptor;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class InitController {


    private final WorkflowServiceStubs workflowServiceStubs;
    private final WorkflowClient workflowClient;

    private final RestController restController;

    public InitController(final WorkflowServiceStubs workflowServiceStubs,
                          final WorkflowClient workflowClient,
                          final RestController restController) {
        this.workflowServiceStubs = workflowServiceStubs;
        this.workflowClient = workflowClient;
        this.restController = restController;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {

        invokeGetSystemInfo();


        final int sleep = 1_000;
        System.out.println("sleep ms : " + sleep);

        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        startWorkflow();

        startWorkflow();

    }

    private void initGetSystemInfo3() {
        final long millis = workflowServiceStubs.getOptions().getHealthCheckAttemptTimeout().toMillis();


        workflowClient.getWorkflowServiceStubs()
                .blockingStub().withDeadline(Deadline.after(
                                millis,
                                //workflowServiceStubs.getOptions().getRpcTimeout().getSeconds(),
                                TimeUnit.MILLISECONDS
                        )

                ).getSystemInfo(
                        GetSystemInfoRequest
                                .newBuilder()
                                .build());
    }

    private void initGetSystemInfo2() {


        final Deadline deadline = Deadline.after(workflowServiceStubs.getOptions().getHealthCheckAttemptTimeout().toMillis(),
                TimeUnit.MILLISECONDS);

        RpcRetryOptions rpcRetryOptions =
                RpcRetryOptions.newBuilder()
                        .setExpiration(
                                Duration.ofMillis(deadline.timeRemaining(TimeUnit.MILLISECONDS)))
                        .validateBuildWithDefaults();
        GrpcRetryer.GrpcRetryerOptions grpcRetryerOptions =
                new GrpcRetryer.GrpcRetryerOptions(rpcRetryOptions, deadline);

        new GrpcRetryer(GetSystemInfoResponse.Capabilities::getDefaultInstance)
                .retryWithResult(
                        () -> SystemInfoInterceptor.getServerCapabilitiesOrThrow(this.workflowServiceStubs.getRawChannel(),
                                deadline),
                        grpcRetryerOptions);

    }

    private void invokeGetSystemInfo() {


        WorkflowServiceStubs.newConnectedServiceStubs(workflowServiceStubs.getOptions(),
                        workflowServiceStubs.getOptions().getHealthCheckAttemptTimeout())
                .blockingStub().getSystemInfo(GetSystemInfoRequest
                        .newBuilder()
                        .build());


    }

    private void startWorkflow() {

        restController.startWorkflow();

    }


}
