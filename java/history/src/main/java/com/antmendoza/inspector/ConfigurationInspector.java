package com.antmendoza.inspector;

import com.antmendoza.loader.ActivityData;
import java.util.List;

public interface ConfigurationInspector {
  List<Tip> inspectActivities(final List<ActivityData> activityDataList);
//TODO
//  List<Tip> inspectWorkflow
//  List<Tip> inspectChilds
//  List<Tip> inspectTimers


}
