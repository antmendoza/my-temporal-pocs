package com.antmendoza.loader;

import com.google.protobuf.Timestamp;
import io.temporal.api.history.v1.ActivityTaskCompletedEventAttributes;
import io.temporal.api.history.v1.ActivityTaskScheduledEventAttributes;
import io.temporal.api.history.v1.ActivityTaskStartedEventAttributes;
import io.temporal.api.history.v1.HistoryEvent;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ActivityDataTest {


    @Test
    public void calculateActivityScheduleLatency() {


        final HistoryEvent activityTaskScheduledEvent = HistoryEvent.newBuilder()
                .setEventId(1)
                .setEventTime(Timestamp.newBuilder()
                        .setSeconds(2).build())
                .setActivityTaskScheduledEventAttributes(ActivityTaskScheduledEventAttributes.newBuilder().build()).build();

        final ActivityData activityData = new ActivityData(activityTaskScheduledEvent);


        assertNotNull(activityData);

        final HistoryEvent activityTaskStartedEvent = HistoryEvent.newBuilder()
                .setEventId(2)
                .setEventTime(Timestamp.newBuilder()
                        .setSeconds(5).build())
                .setActivityTaskStartedEventAttributes(ActivityTaskStartedEventAttributes.newBuilder().build()).build();

        activityData.addActivityTaskStartedEvent(activityTaskStartedEvent);

        assertEquals(3, activityData.scheduleToStartLatency());

        final HistoryEvent activityTaskCompletedEvent = HistoryEvent.newBuilder()
                .setEventId(3)
                .setEventTime(Timestamp.newBuilder().setSeconds(10).build())
                .setActivityTaskCompletedEventAttributes(ActivityTaskCompletedEventAttributes.newBuilder().build()).build();

        activityData.addActivityTaskFinalEvent(activityTaskCompletedEvent);


        assertEquals(5, activityData.startToCloseLatency());
    }
}