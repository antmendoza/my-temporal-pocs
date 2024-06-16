package com.antmendoza.inspector;

import com.antmendoza.StartToCloseLatencyConfInspector;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationInspectorFactory {

  private final List<ConfigurationInspector> configurationInspectors = new ArrayList<>();

  public ConfigurationInspectorFactory() {
    this.configurationInspectors.add(new StartToCloseLatencyConfInspector());
  }

  public List<ConfigurationInspector> getConfigurationInspectors() {
    return configurationInspectors;
  }
}
