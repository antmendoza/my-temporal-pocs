package org.example.workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface NewspaperCriticWf {
    @WorkflowMethod
    void startCriticWf(long userId);

    @SignalMethod
    void receiveNewspaper(String title);

    @SignalMethod
    void secretaryFinished(String newspaperTitle);

    @QueryMethod
    WfState queryState();
}
