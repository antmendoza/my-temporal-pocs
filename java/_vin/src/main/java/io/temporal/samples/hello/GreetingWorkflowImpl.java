package io.temporal.samples.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowLock;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GreetingWorkflowImpl implements GreetingWorkflow {


    private boolean unblock;

    @Override
    public String mainMethod(String name) {

        Workflow.await(()-> unblock);


        return "hello";
    }

    @Override
    public String getNotification(String notification) {
        this.unblock = true;

        return "";
    }


}
