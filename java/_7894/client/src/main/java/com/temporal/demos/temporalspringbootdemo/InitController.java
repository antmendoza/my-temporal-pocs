package com.temporal.demos.temporalspringbootdemo;

import io.temporal.api.workflowservice.v1.GetSystemInfoRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class InitController {


    private final WorkflowServiceStubs workflowServiceStubs;

    private final RestController restController;

    public InitController(final WorkflowServiceStubs workflowServiceStubs, final WorkflowClient workflowClient, final RestController restController) {
        this.workflowServiceStubs = workflowServiceStubs;
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

    private void invokeGetSystemInfo() {
        Date d1 = new Date();


        WorkflowServiceStubs.newConnectedServiceStubs(workflowServiceStubs.getOptions(),
                        workflowServiceStubs.getOptions().getHealthCheckAttemptTimeout())
                .blockingStub().getSystemInfo(GetSystemInfoRequest
                        .newBuilder()
                        .build());


        Date d2 = new Date();

        long ms = (d2.getTime() - d1.getTime());
        System.out.println("first GetSystemInfoRequest ms : " + ms);
    }

    private void startWorkflow() {

        restController.startWorkflow();

    }


}
