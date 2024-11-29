package com.example.service2.api;

import com.example.service2.TraceUtils;
import com.example.service2.worker.WorkerStarter;
import com.example.service2.workflows.TestWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.opentracing.OpenTracingClientInterceptor;
import io.temporal.serviceclient.WorkflowServiceStubs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("v1/test")
public class TestController {

    private final WorkflowClient client;

    public TestController() {
        this.client = createWorkflowClient();
    }

    @GetMapping("tracing")
    public String testTracing() {


        log.info("Starting worker");
        new WorkerStarter().run();

        log.info("Received request from Service 1");

        TestWorkflow workflow = createWorkflow();

        String result = workflow.callTestActivity();

        return result;


    }

    private TestWorkflow createWorkflow() {
        WorkflowOptions options = createWorkflowOptions();
        return client.newWorkflowStub(TestWorkflow.class, options);
    }

    private WorkflowClient createWorkflowClient() {
        WorkflowServiceStubs serviceStubs = createWorkflowServiceStubs();


        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setInterceptors(new OpenTracingClientInterceptor(
                                        TraceUtils.getOptions()
                                )
                        )
                        .build();

        return WorkflowClient.newInstance(serviceStubs, clientOptions);
    }

    private WorkflowServiceStubs createWorkflowServiceStubs() {
        return WorkflowServiceStubs.newLocalServiceStubs();
    }

    private WorkflowOptions createWorkflowOptions() {
        return WorkflowOptions.newBuilder()
                .setTaskQueue("TASK_QUEUE")
                .build();
    }
}