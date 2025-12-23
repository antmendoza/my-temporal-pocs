package com.example.servicespringboot.api;

import com.example.servicespringboot.workflows.TestWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("v1/test")
public class TestController {

    @Autowired
    private WorkflowClient workflowClient;

    @GetMapping("tracing")
    public String testTracing() {


        log.info("Received request from Service 1");

        TestWorkflow workflow = createWorkflow();

        String result = workflow.callTestActivity();

        return result;


    }

    private TestWorkflow createWorkflow() {
        WorkflowOptions options = createWorkflowOptions();
        return workflowClient.newWorkflowStub(TestWorkflow.class, options);
    }


    private WorkflowOptions createWorkflowOptions() {
        return WorkflowOptions.newBuilder()
                .setTaskQueue("TASK_QUEUE")
                .build();
    }
}