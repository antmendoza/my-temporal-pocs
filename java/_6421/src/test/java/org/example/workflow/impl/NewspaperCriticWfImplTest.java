package org.example.workflow.impl;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.common.converter.JacksonJsonPayloadConverter;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import io.temporal.worker.Worker;
import org.example.workflow.NewspaperCriticWf;
import org.example.workflow.ReviewActivity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.workflow.Constants.CRITIC_PERIOD_SEC;
import static org.example.workflow.Constants.CRITIC_RESOLUTION;
import static org.example.workflow.Constants.SECRETARY_PERIOD_SEC;
import static org.example.workflow.Constants.SECRETARY_RESOLUTION;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NewspaperCriticWfImplTest {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @RegisterExtension
    public static final TestWorkflowExtension extension = TestWorkflowExtension.newBuilder()
            .setWorkflowTypes(NewspaperCriticWfImpl.class, NewspaperSecretaryWfImpl.class)
            .setNamespace("criticism")
            .setWorkflowClientOptions(WorkflowClientOptions.newBuilder()
                    .setDataConverter(new DefaultDataConverter(new JacksonJsonPayloadConverter()))
                    .build())
            .useInternalService()
            .setDoNotStart(true)
            .build();

    @Test
    void testCritic(TestWorkflowEnvironment testEnv) {
        ReviewActivity reviewActivity = Mockito.mock(ReviewActivity.class);
        doNothing().when(reviewActivity).submitReview(anyString(), anyString());

        Worker worker1 = testEnv.newWorker("some-queue");
        worker1.registerWorkflowImplementationTypes(NewspaperCriticWfImpl.class);
        worker1.registerActivitiesImplementations(reviewActivity);

        testEnv.start();

        NewspaperCriticWf workflow = testEnv.getWorkflowClient().newWorkflowStub(NewspaperCriticWf.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue("some-queue")
                        .build());

        WorkflowClient.start(workflow::startCriticWf, 1L);

        workflow.receiveNewspaper("title-one");
        waitAndLog(testEnv, Duration.ofMinutes(workflow.queryState().clubbingPeriodMinutes));

        var marginSec = 30;
        var wait1Sec = CRITIC_PERIOD_SEC - marginSec;
        var wait2Sec = marginSec;
        waitAndLog(testEnv, Duration.ofSeconds(wait1Sec));
        verify(reviewActivity, never()).submitReview(anyString(), anyString());
        waitAndLog(testEnv, Duration.ofSeconds(wait2Sec));

        waitAndLog(testEnv, Duration.ofSeconds(2));
        verify(reviewActivity, times(1)).submitReview(anyString(), anyString());
        verify(reviewActivity, times(1)).submitReview("title-one", CRITIC_RESOLUTION);
        assertThat(workflow.queryState().startedMs).isNotNull();
        assertThat(workflow.queryState().finishedMs).isNotNull();
    }

    private void waitAndLog(TestWorkflowEnvironment testEnv, Duration duration) {
        log.warn("test will sleep for {}...", duration);
        testEnv.sleep(duration);
        log.warn("test has slept for {}.", duration);
    }

    @Test
    void testSecretariesNoWait2(TestWorkflowEnvironment testEnv, Worker worker, NewspaperCriticWf workflow) {
        testSecretaries(testEnv, worker, workflow, false);
    }

    @Test
    void testSecretariesWithWait2(TestWorkflowEnvironment testEnv, Worker worker, NewspaperCriticWf workflow) {
        testSecretaries(testEnv, worker, workflow, true);
    }

    private void testSecretaries(TestWorkflowEnvironment testEnv, Worker worker, NewspaperCriticWf workflow, boolean withWait2) {
        ReviewActivity reviewActivity = Mockito.mock(ReviewActivity.class);
        doNothing().when(reviewActivity).submitReview(anyString(), anyString());
        worker.registerActivitiesImplementations(reviewActivity);
        testEnv.start();
        WorkflowClient.start(workflow::startCriticWf, 1L);
        final var virtualStartMs = testEnv.currentTimeMillis();

        workflow.receiveNewspaper("title-one");
        var clubbingOffsetSec = 30;
        var wait1Sec = TimeUnit.MINUTES.toSeconds(workflow.queryState().clubbingPeriodMinutes) - clubbingOffsetSec;
        var wait2Sec = withWait2 ? clubbingOffsetSec : 0;
        testEnv.registerDelayedCallback(
                Duration.ofSeconds(wait1Sec),
                () -> {
                    workflow.receiveNewspaper("title-two");
                }
        );

        Duration reviewWait1Dur = Duration.ofSeconds(SECRETARY_PERIOD_SEC - 15);
        Duration reviewWait2Dur = Duration.ofSeconds(10);
        Duration reviewWait3Dur = Duration.ofSeconds(2);
        Duration overallWait = reviewWait1Dur.plus(reviewWait2Dur).plus(reviewWait3Dur);
        assertThat(overallWait).isLessThan(Duration.ofSeconds(SECRETARY_PERIOD_SEC));
        assertThat(overallWait).isPositive();
        testEnv.registerDelayedCallback(
                Duration.ofSeconds(wait1Sec).plusSeconds(wait2Sec).plus(reviewWait1Dur),
                () -> {
                    log.info("test has slept for {}, sending title-two, sinceStart={}",
                            Duration.ofSeconds(wait2Sec).plus(reviewWait1Dur),
                            Duration.ofMillis(testEnv.currentTimeMillis() - virtualStartMs)
                    );
                    workflow.secretaryFinished("title-two");
                }
        );
        testEnv.registerDelayedCallback(
                Duration.ofSeconds(wait1Sec).plusSeconds(wait2Sec).plus(reviewWait1Dur).plus(reviewWait2Dur),
                () -> {
                    log.info("test has slept for {}, sending title-one, sinceStart={}",
                            reviewWait2Dur,
                            Duration.ofMillis(testEnv.currentTimeMillis() - virtualStartMs)
                    );
                    verify(reviewActivity, never()).submitReview(anyString(), anyString());
                    workflow.secretaryFinished("title-one");
                }
        );

        WorkflowStub.fromTyped(workflow).getResult(Void.class);

        InOrder io = inOrder(reviewActivity);
        io.verify(reviewActivity).submitReview("title-two", SECRETARY_RESOLUTION);
        io.verify(reviewActivity).submitReview("title-one", SECRETARY_RESOLUTION);
        io.verifyNoMoreInteractions();

        assertThat(workflow.queryState().startedMs).isNotNull();
        assertThat(workflow.queryState().finishedMs).isNotNull();
    }
}