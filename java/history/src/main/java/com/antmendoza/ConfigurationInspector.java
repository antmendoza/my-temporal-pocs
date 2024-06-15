package com.antmendoza;

import com.antmendoza.loader.ActivityData;

import java.util.ArrayList;
import java.util.List;

public interface ConfigurationInspector {
     List<Tip> inspectActivities(final List<ActivityData> activityDataList);
}
