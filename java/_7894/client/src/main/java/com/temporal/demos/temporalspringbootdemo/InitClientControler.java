package com.temporal.demos.temporalspringbootdemo;

import io.grpc.Deadline;
import io.temporal.api.workflowservice.v1.GetSystemInfoRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class InitClientControler {


    private final WorkflowServiceStubs workflowServiceStubs;
    private final WorkflowClient workflowClient;

    public InitClientControler(final WorkflowServiceStubs workflowServiceStubs, final WorkflowClient workflowClient) {
        this.workflowServiceStubs = workflowServiceStubs;
        this.workflowClient = workflowClient;
    }

    // @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {

        Date d1 = new Date();



        System.out.println("workflowServiceStubs getRpcTimeout(): " + workflowServiceStubs.getOptions().getRpcTimeout());




        final long millis = workflowServiceStubs.getOptions().getHealthCheckAttemptTimeout().toMillis();

        System.out.println("millis getHealthCheckAttemptTimeout(): " + millis);


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





        Date d2 = new Date();

        long seconds = (d2.getTime()-d1.getTime());
        System.out.println("doSomethingAfterStartup : " + seconds);

    }


}
