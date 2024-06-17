package org.example.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface NewspaperSecretaryWf {
    @WorkflowMethod
    void startSecretaryWf(String title);
}
