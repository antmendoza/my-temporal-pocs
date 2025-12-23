package org.example.workflow.impl;

import io.temporal.workflow.Workflow;
import org.example.workflow.NewspaperCriticWf;
import org.example.workflow.NewspaperSecretaryWf;
import org.slf4j.Logger;

import java.time.Duration;

import static org.example.workflow.Constants.SECRETARY_PERIOD_SEC;
import static org.example.workflow.Constants.SINGLETON_NAME;

public class NewspaperSecretaryWfImpl implements NewspaperSecretaryWf {
    private Logger log = io.temporal.workflow.Workflow.getLogger(this.getClass());

    @Override
    public void startSecretaryWf(String title) {
        log.info("secretary is sleeping upon $title...");
        Workflow.sleep(Duration.ofSeconds(SECRETARY_PERIOD_SEC));
        log.info("secretary has slept upon $title.");
        Workflow.newUntypedExternalWorkflowStub(SINGLETON_NAME)
                .signal(assertMethodExists(NewspaperCriticWf.class, "secretaryFinished"), title);
    }

    private String assertMethodExists(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName, String.class).getName();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

