package io.temporal.samples.earlyreturn;

import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.workflow.TimerOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionWorkflowImpl implements TransactionWorkflow {
  private static final Logger log = LoggerFactory.getLogger(TransactionWorkflowImpl.class);
  private final TransactionActivities activities =
      Workflow.newActivityStub(
          TransactionActivities.class,
          ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(30)).build());

  private final TransactionActivities localActivities =
      Workflow.newLocalActivityStub(
          TransactionActivities.class,
          LocalActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

  private boolean initDone = false;
  private Transaction tx;
  private Exception initError = null;

  @Override
  public TxResult processTransaction(TransactionRequest txRequest) {

    this.tx = activities.mintTransactionId(txRequest);

    try {
      this.tx = activities.initTransaction(this.tx);
    } catch (Exception e) {
      initError = e;
    } finally {
      initDone = true;
    }

    // simulate some processing time
    Workflow.newTimer(
        Duration.ofSeconds(30),
        TimerOptions.newBuilder().setSummary("simulate some processing time").build());

    return null;
  }

  @Override
  public TxResult returnInitResult() {
    Workflow.await(() -> initDone);

    if (initError != null) {
      log.info("Initialization failed.");
      throw Workflow.wrap(initError);
    }

    return new TxResult(tx.getId(), "Initialization successful");
  }
}
