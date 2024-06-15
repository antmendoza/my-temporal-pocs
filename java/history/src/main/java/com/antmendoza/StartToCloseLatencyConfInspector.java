package com.antmendoza;

import com.antmendoza.loader.ActivityData;

import java.util.ArrayList;
import java.util.List;

public class StartToCloseLatencyConfInspector implements ConfigurationInspector {
    @Override
    public List<Tip> inspectActivities(final List<ActivityData> activityDataList) {

        final List<Tip> tips = new ArrayList<>();

        activityDataList.forEach(ac -> {

            tips.add(new Tip(
                    ConfigurationProperty.ActivityStartToClose,
                    ActionSuggested.ReduceValue,
                    ac.startToCloseConfigValue(),
                    ac.startToCloseLatency()));

        });

        return tips;

    }
}

