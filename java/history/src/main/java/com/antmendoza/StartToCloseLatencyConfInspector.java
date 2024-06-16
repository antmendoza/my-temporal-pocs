package com.antmendoza;

import com.antmendoza.inspector.ConfigurationInspector;
import com.antmendoza.inspector.ConfigurationProperty;
import com.antmendoza.inspector.Tip;
import com.antmendoza.loader.ActivityData;
import java.util.ArrayList;
import java.util.List;

public class StartToCloseLatencyConfInspector implements ConfigurationInspector {
  @Override
  public List<Tip> inspectActivities(final List<ActivityData> activityDataList) {

    final List<Tip> tips = new ArrayList<>();

    activityDataList.forEach(
        ac -> {
          if (ac.startToCloseConfigValue().getSeconds()
              > ac.startToCloseLatency().getSeconds() * 1.2)
            tips.add(
                new Tip(
                    ac.entityDescription(),
                    ConfigurationProperty.ActivityStartToClose,
                    "activityStartToClose configured valued is too high."
                        + " Set the value to the maximum time the activity execution can take",
                    ac.startToCloseConfigValue(),
                    ac.startToCloseLatency()));
        });

    return tips;
  }
}
