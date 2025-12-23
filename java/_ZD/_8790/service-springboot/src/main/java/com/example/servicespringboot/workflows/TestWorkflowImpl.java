package com.example.servicespringboot.workflows;

import com.example.servicespringboot.activities.TestActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@WorkflowImpl(taskQueues = {"TASK_QUEUE"})
public class TestWorkflowImpl implements TestWorkflow {

    private final TestActivity testActivity = Workflow.newActivityStub(TestActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(3))
                    //.setTaskQueue("TASK_QUEUE")
                    .build());

    @Override
    public String callTestActivity() {
        return testActivity.printHello();
    }
}
