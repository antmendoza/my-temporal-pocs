package com.antmendoza.loader;

import io.temporal.api.history.v1.HistoryEvent;

import java.time.Duration;

public class ActivityData {

    private final long activityTaskScheduledId;
    private final HistoryEvent activityTaskScheduled;
    private HistoryEvent activityTaskStarted;
    private HistoryEvent activityTaskFinalEvent;

    public ActivityData(final HistoryEvent activityTaskScheduled) {
        this.activityTaskScheduled = activityTaskScheduled;
        this.activityTaskScheduledId = this.activityTaskScheduled.getEventId();
    }


    public boolean hasActivityTaskScheduledEvent(long eventId) {
        return this.activityTaskScheduledId == eventId;
    }

    public void addActivityTaskStartedEvent(final HistoryEvent activityTaskStarted) {
        this.activityTaskStarted = activityTaskStarted;
    }


    public void addActivityTaskFinalEvent(final HistoryEvent activityTaskFinalEvent) {
        this.activityTaskFinalEvent = activityTaskFinalEvent;
    }

    public long scheduleToStartLatency() {
        return activityTaskStarted.getEventTime().getSeconds() -
                activityTaskScheduled.getEventTime().getSeconds();

    }

    public Duration startToCloseLatency() {

        final long nanoseconds = activityTaskFinalEvent.getEventTime().getNanos() -
                activityTaskStarted.getEventTime().getNanos();
        return Duration.ofMillis(nanoseconds / 1_000_000);

    }


    public Duration startToCloseConfigValue() {

        return Duration.ofSeconds( activityTaskScheduled
                .getActivityTaskScheduledEventAttributes().getStartToCloseTimeout().getSeconds());


    }
}
