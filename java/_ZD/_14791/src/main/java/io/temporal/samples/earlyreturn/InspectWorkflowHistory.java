package io.temporal.samples.earlyreturn;

import com.google.protobuf.Timestamp;
import io.temporal.api.enums.v1.EventType;
import io.temporal.api.history.v1.HistoryEvent;
import io.temporal.client.WorkflowClient;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    Timestamp start = events.get(2).getEventTime();
    Timestamp end = events.get(3).getEventTime();
    return toMillis(end) - toMillis(start);
  }

  /**
   * Returns execution latency (ActivityTaskStarted → ActivityTaskCompleted) per activity type.
   * Correlates events via scheduledEventId / startedEventId so the activity type name, which is
   * only present on ActivityTaskScheduled, can be associated with the completion event.
   */
  public Map<String, Long> getActivityLatenciesMillis() {
    Map<Long, String> scheduledIdToType = new LinkedHashMap<>();
    Map<Long, Timestamp> startedIdToTime = new LinkedHashMap<>();
    Map<String, Long> result = new LinkedHashMap<>();

    for (HistoryEvent event : events) {
      switch (event.getEventType()) {
        case EVENT_TYPE_ACTIVITY_TASK_SCHEDULED:
          scheduledIdToType.put(
              event.getEventId(),
              event.getActivityTaskScheduledEventAttributes().getActivityType().getName());
          break;
        case EVENT_TYPE_ACTIVITY_TASK_STARTED:
          startedIdToTime.put(event.getEventId(), event.getEventTime());
          break;
        case EVENT_TYPE_ACTIVITY_TASK_COMPLETED:
          {
            long scheduledEventId =
                event.getActivityTaskCompletedEventAttributes().getScheduledEventId();
            long startedEventId =
                event.getActivityTaskCompletedEventAttributes().getStartedEventId();
            String activityType = scheduledIdToType.get(scheduledEventId);
            Timestamp startTime = startedIdToTime.get(startedEventId);
            if (activityType != null && startTime != null) {
              result.put(activityType, toMillis(event.getEventTime()) - toMillis(startTime));
            }
            break;
          }
        default:
          break;
      }
    }
    return result;
  }

  private static long toMillis(Timestamp t) {
    return t.getSeconds() * 1000 + t.getNanos() / 1_000_000;
  }
}
