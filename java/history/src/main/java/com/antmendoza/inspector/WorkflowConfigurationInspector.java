package com.antmendoza.inspector;

import com.antmendoza.loader.WorkflowExecutionHistoryData;
import java.util.List;

public class WorkflowConfigurationInspector {

  private final List<ConfigurationInspector> inspectorList;
  private WorkflowExecutionHistoryData workflowExecutionHistoryData;

  public WorkflowConfigurationInspector(
      final WorkflowExecutionHistoryData workflowExecutionHistoryData) {

    this.inspectorList = new ConfigurationInspectorFactory().getConfigurationInspectors();
    this.workflowExecutionHistoryData = workflowExecutionHistoryData;
  }

  public ConfigurationInspectorResult feedback() {

    ConfigurationInspectorResult configurationInspectorResult = new ConfigurationInspectorResult();

    this.inspectorList.forEach(
        ad -> {
          configurationInspectorResult.addTip(
              ad.inspectActivities(this.workflowExecutionHistoryData.getActivityDataList()));
        });

    return configurationInspectorResult;
  }
}
