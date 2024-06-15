package com.antmendoza;

import io.temporal.common.WorkflowExecutionHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkflowExecutionHistoryData {
    private final WorkflowExecutionHistory workflowExecutionHistory;


    private final List<ActivityData> activityDataList = new ArrayList<>();

    public WorkflowExecutionHistoryData(final WorkflowExecutionHistory workflowExecutionHistory) {

        this.workflowExecutionHistory = workflowExecutionHistory;

        this.load();
    }

    private void load() {

        loadActivities();
//        loadChildWf();

    }

    private void loadActivities() {


        this.workflowExecutionHistory.getHistory().getEventsList().forEach(e -> {

            if (e.hasActivityTaskScheduledEventAttributes()) {
                activityDataList.add(new ActivityData(e));
            }

            if (e.hasActivityTaskStartedEventAttributes()) {
                Optional<ActivityData> activityData = this.activityDataList.stream().filter(ad ->
                        ad.hasActivityTaskScheduledEvent(e.getEventId())).findFirst();

                if (activityData.isPresent()) {
                    activityData.get().addActivityTaskStartedEvent(e);
                }

            }


            if (e.hasActivityTaskStartedEventAttributes()
            ) {
                Optional<ActivityData> activityData = this.activityDataList.stream().filter(ad ->
                        ad.hasActivityTaskScheduledEvent(e.getEventId())).findFirst();

                if (activityData.isPresent()) {
                    activityData.get().addActivityTaskStartedEvent(e);
                }

            }


            if (e.hasActivityTaskCanceledEventAttributes()
                            || e.hasActivityTaskCompletedEventAttributes()
                            || e.hasActivityTaskFailedEventAttributes()
                            || e.hasActivityTaskTimedOutEventAttributes()
            ) {
                Optional<ActivityData> activityData = this.activityDataList.stream().filter(ad ->
                        ad.hasActivityTaskScheduledEvent(e.getEventId())).findFirst();

                if (activityData.isPresent()) {
                    activityData.get().addActivityTaskFinalEvent(e);
                }

            }
        });

    }


    public List<ActivityData> getActivityDataList() {
        return activityDataList;
    }
}
