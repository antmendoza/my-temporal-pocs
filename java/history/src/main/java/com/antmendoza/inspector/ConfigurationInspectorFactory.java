package com.antmendoza.inspector;

import com.antmendoza.StartToCloseLatencyConfInspector;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationInspectorFactory {

  private final List<ConfigurationInspector> configurationInspectorList = new ArrayList<>();

  public ConfigurationInspectorFactory() {
    this.configurationInspectorList.add(new StartToCloseLatencyConfInspector());
  }

  public List<ConfigurationInspector> getConfigurationAdvisorList() {
    return configurationInspectorList;
  }
}
