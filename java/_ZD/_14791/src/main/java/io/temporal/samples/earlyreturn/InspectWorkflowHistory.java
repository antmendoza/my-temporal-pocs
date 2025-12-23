package io.temporal.samples.earlyreturn;

import com.google.protobuf.Timestamp;
import io.temporal.api.history.v1.HistoryEvent;
import io.temporal.client.WorkflowClient;
import java.util.List;

public class InspectWorkflowHistory {
  private final String workflowId;
  private final WorkflowClient client;
  private final List<HistoryEvent> events;

  public InspectWorkflowHistory(WorkflowClient client, String workflowId) {
    this.client = client;
    this.workflowId = workflowId;
    // The latency we see in workflow history is the for workflow task execution latency (from start
    // to completion)
    this.events = client.fetchHistory(workflowId).getEvents();
  }

  public long getFirstWorkflowTaskLatencyMillis() {

    Timestamp workflowTaskStartTime = events.get(2).getEventTime();
    Timestamp workflowTaskCompletionTime = events.get(3).getEventTime();
    long startMillis =
        workflowTaskStartTime.getSeconds() * 1000 + workflowTaskStartTime.getNanos() / 1_000_000;
    long endMillis =
        workflowTaskCompletionTime.getSeconds() * 1000
            + workflowTaskCompletionTime.getNanos() / 1_000_000;
    long latencyMillis = endMillis - startMillis;
    return latencyMillis;
  }
}
