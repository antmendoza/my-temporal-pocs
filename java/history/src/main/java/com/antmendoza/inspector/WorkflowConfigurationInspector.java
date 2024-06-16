package com.antmendoza.inspector;

import com.antmendoza.loader.WorkflowExecutionHistoryData;
import java.util.List;

public class WorkflowConfigurationInspector {

  private final List<ConfigurationInspector> advisorList;
  private WorkflowExecutionHistoryData workflowExecutionHistoryData;

  public WorkflowConfigurationInspector(
      final WorkflowExecutionHistoryData workflowExecutionHistoryData) {

    this.advisorList = new ConfigurationInspectorFactory().getConfigurationAdvisorList();
    this.workflowExecutionHistoryData = workflowExecutionHistoryData;
  }

  public ConfigurationInspectorResult feedback() {

    ConfigurationInspectorResult configurationInspectorResult = new ConfigurationInspectorResult();

    this.advisorList.forEach(
        ad -> {
          configurationInspectorResult.addTip(
              ad.inspectActivities(this.workflowExecutionHistoryData.getActivityDataList()));
        });

    return configurationInspectorResult;
  }
}
