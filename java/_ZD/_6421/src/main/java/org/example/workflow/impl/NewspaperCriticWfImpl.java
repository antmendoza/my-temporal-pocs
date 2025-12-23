package org.example.workflow.impl;

import io.temporal.activity.ActivityOptions;
import io.temporal.api.enums.v1.ParentClosePolicy;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;
import org.example.workflow.Constants;
import org.example.workflow.NewspaperCriticWf;
import org.example.workflow.NewspaperSecretaryWf;
import org.example.workflow.ReviewActivity;
import org.example.workflow.WfState;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Supplier;

import static org.example.workflow.Constants.CRITIC_PERIOD_SEC;
import static org.example.workflow.Constants.CRITIC_RESOLUTION;
import static org.example.workflow.Constants.SECRETARY_PERIOD_SEC;

public class NewspaperCriticWfImpl implements NewspaperCriticWf {
    private Logger log = Workflow.getLogger(this.getClass());

    private ArrayList<String> receivedTitles = new ArrayList<>();
    private boolean didReceiveWork = false;

    private ArrayList<String> receivedFromSecretary = new ArrayList<>();
    private WfState wfState = new WfState();

    private static final ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(4)) // Currently, only set StartToClose based on https://docs.temporal.io/docs/concepts/activities/#start-to-close
            .setRetryOptions(
                    RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setMaximumInterval(Duration.ofSeconds(5))
                            .setBackoffCoefficient(2.0)
                            .setMaximumAttempts(0)
                            .setDoNotRetry(IllegalStateException.class.getName())
                            .build()
            )
            .build();


    @Override
    public void startCriticWf(long unused) {
        wfState.startedMs = Workflow.currentTimeMillis();
        startCriticWf0();
        wfState.finishedMs = Workflow.currentTimeMillis();
    }

    private void startCriticWf0() {
        final var startMs = Workflow.currentTimeMillis();
        var reviewActivity = Workflow.newActivityStub(ReviewActivity.class, options);
        var random = Workflow.newRandom();
        log.info("sleeping for 7d...");
        check(isAwait(Duration.ofDays(7), () -> didReceiveWork, startMs), "should have quit on condition, not timeout");
        didReceiveWork = false;

        check(wfState.clubbingPeriodMinutes == null, "unexpected state");
        wfState.clubbingPeriodMinutes = 10L + random.nextInt(50);
        wfState.clubbingPeriodMinutes = 42L; // temporary assignment for better determinism
        log.info("sleeping {}m...", wfState.clubbingPeriodMinutes);
        isAwait(Duration.ofMinutes(wfState.clubbingPeriodMinutes), () -> false, startMs);
        didReceiveWork = true;
        log.info("slept for {}m, received titles: {}", wfState.clubbingPeriodMinutes, receivedTitles);

        if (receivedTitles.size() == 1) { // the critic works alone
            log.info("the critic will be working for {}sec...", CRITIC_PERIOD_SEC);
            isAwait(Duration.ofSeconds(CRITIC_PERIOD_SEC), () -> false, startMs);
            log.info("the critic finished");
            var finishedTitle = receivedTitles.get(0);
            reviewActivity.submitReview(finishedTitle, CRITIC_RESOLUTION);
        } else if (receivedTitles.size() > 1) { // the critic dispatches the work
            for (int i = 0; i < receivedTitles.size(); i++) {
                var workflowOptions = ChildWorkflowOptions.newBuilder()
                        .setWorkflowId("secretary-" + i)
                        //.setTaskQueue("some-queue")
                        //.setTaskQueue(Workflow.getInfo().getTaskQueue())
                        .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_ABANDON)
                        .build();
                var secretaryWf = Workflow.newChildWorkflowStub(NewspaperSecretaryWf.class, workflowOptions);
                Async.procedure(secretaryWf::startSecretaryWf, receivedTitles.get(i));
                var promise = Workflow.getWorkflowExecution(secretaryWf);
                promise.get();
            }
            log.info("sleeping {}sec or until everything is reviewed", SECRETARY_PERIOD_SEC);
            var per = Duration.ofSeconds(SECRETARY_PERIOD_SEC).plusSeconds(5);
            var quitOnCondition = isAwait(per, () -> receivedFromSecretary.size() == receivedTitles.size(), startMs);
            check(quitOnCondition, String.format("should have received %d titles back from secretaries, but received %d", receivedTitles.size(), receivedFromSecretary.size()));
            log.info("received everything from secretaries");
            receivedFromSecretary.forEach(title -> reviewActivity.submitReview(title, Constants.SECRETARY_RESOLUTION));
            log.info("relayed everything from secretaries");
        } else fail();
    }

    private boolean isAwait(Duration duration, Supplier<Boolean> exitCondition, long startMs) {
        log.warn("PROD will sleep for {}... sinceStart={}", duration, Duration.ofMillis(Workflow.currentTimeMillis() - startMs));
        boolean result = Workflow.await(duration, exitCondition);
        log.warn("PROD has slept for {}. sinceStart={}", duration, Duration.ofMillis(Workflow.currentTimeMillis() - startMs));
        return result;
    }

    @Override
    public void receiveNewspaper(String title) {
        check(!receivedTitles.contains(title), "should not receive duplicate work");
        receivedTitles.add(title);
        didReceiveWork = true;
    }

    @Override
    public void secretaryFinished(String newspaperTitle) {
        check(!receivedFromSecretary.contains(newspaperTitle), "should not receive duplicates from secretaries");
        receivedFromSecretary.add(newspaperTitle);
    }

    @Override
    public WfState queryState() {
        return wfState;
    }

    private static void fail() {
        check(false, "constant failure");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new RuntimeException(message);
    }
}
