package io.temporal.samples.earlyreturn;

public class TransactionActivitiesImpl implements TransactionActivities {

  @Override
  public Transaction mintTransactionId(TransactionRequest request) {
    String txId = "TXID" + String.format("%010d", (long) (Math.random() * 1_000_000_0000L));
    sleep(100);
    return new Transaction(
        txId, request.getSourceAccount(), request.getTargetAccount(), request.getAmount());
  }

  @Override
  public Transaction initTransaction(Transaction tx) {
    sleep(100);
    return tx;
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
