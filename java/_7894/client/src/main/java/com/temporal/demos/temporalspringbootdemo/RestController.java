package com.temporal.demos.temporalspringbootdemo;

import com.temporal.demos.temporalspringbootdemo.workflows.DemoWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Date;

@org.springframework.web.bind.annotation.RestController
public class RestController {



    private final WorkflowServiceStubs workflowServiceStubs;
    private final WorkflowClient workflowClient;

    public RestController(final WorkflowServiceStubs workflowServiceStubs, final WorkflowClient workflowClient) {
        this.workflowServiceStubs = workflowServiceStubs;
        this.workflowClient = workflowClient;
    }


    @PostMapping("/start")
    String newEmployee() {


        System.out.println("workflowServiceStubs getRpcTimeout()" + workflowServiceStubs.getOptions().getRpcTimeout());

        DemoWorkflow workflow = workflowClient.newWorkflowStub(DemoWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId("my-workflow"+Math.random())
                        .setTaskQueue("DemoTaskQueue")
                        .build());



        Date d1 = new Date();


        WorkflowExecution workflowInstance = WorkflowClient.start(workflow::exec, "");


        Date d2 = new Date();

        long seconds = (d2.getTime()-d1.getTime());
        System.out.println("Request ms: " + seconds);


        return workflowInstance.getWorkflowId();
    }


    @GetMapping("/test")
    String get() {

        return "done";

    }


}
