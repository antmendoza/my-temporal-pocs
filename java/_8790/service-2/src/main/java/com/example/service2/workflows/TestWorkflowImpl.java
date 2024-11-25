package com.example.service2.workflows;

import com.example.service2.activities.TestActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.time.Duration;

@Slf4j
public class TestWorkflowImpl implements TestWorkflow {
    private final TestActivity testActivity = Workflow.newActivityStub(TestActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(3))
                    .setTaskQueue("TASK_QUEUE_3").build());

    @Override
    public String callTestActivity() {
        log.info("Calling test activity");
        return testActivity.printHello();
    }
}
