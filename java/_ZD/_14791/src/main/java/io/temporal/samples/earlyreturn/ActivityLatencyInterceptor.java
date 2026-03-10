package io.temporal.samples.earlyreturn;

import io.temporal.activity.ActivityExecutionContext;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptorBase;
import io.temporal.common.interceptors.WorkerInterceptorBase;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityLatencyInterceptor extends WorkerInterceptorBase {

  // activityType -> start/end timestamps (last observed execution per type)
  private final Map<String, Instant> startTimes = new ConcurrentHashMap<>();
  private final Map<String, Instant> endTimes = new ConcurrentHashMap<>();

  public void reset() {
    startTimes.clear();
    endTimes.clear();
  }

  /** Returns latency in millis per activity type for the last measured run. */
  public Map<String, Long> getLatenciesMillis() {
    Map<String, Long> result = new LinkedHashMap<>();
    for (String activityType : startTimes.keySet()) {
      Instant start = startTimes.get(activityType);
      Instant end = endTimes.get(activityType);
      if (start != null && end != null) {
        result.put(activityType, end.toEpochMilli() - start.toEpochMilli());
      }
    }
    return result;
  }

  @Override
  public ActivityInboundCallsInterceptor interceptActivity(ActivityInboundCallsInterceptor next) {
    return new ActivityInboundCallsInterceptorBase(next) {

      private String activityType;

      @Override
      public void init(ActivityExecutionContext context) {
        activityType = context.getInfo().getActivityType();
        super.init(context);
      }

      @Override
      public ActivityOutput execute(ActivityInput input) {
        startTimes.put(activityType, Instant.now());
        try {
          ActivityOutput result = super.execute(input);
          endTimes.put(activityType, Instant.now());
          return result;
        } catch (Exception e) {
          endTimes.put(activityType, Instant.now());
          throw e;
        }
      }
    };
  }
}
