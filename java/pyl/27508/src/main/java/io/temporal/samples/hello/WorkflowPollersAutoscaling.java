package io.temporal.samples.hello;

import io.temporal.worker.tuning.PollerBehavior;
import io.temporal.worker.tuning.PollerBehaviorAutoscaling;

public class WorkflowPollersAutoscaling {
    public PollerBehavior toPollerBehavior() {
        return new PollerBehaviorAutoscaling();
    }
}
