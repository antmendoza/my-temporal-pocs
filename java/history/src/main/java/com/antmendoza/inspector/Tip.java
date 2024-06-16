package com.antmendoza.inspector;

import java.util.Objects;

public class Tip {
  private String description;
  private final ConfigurationProperty configurationProperty;
  private final String actionSuggested;
  private final Object configuredValue;
  private final Object currentValue;

  public Tip(
      final String description,
      final ConfigurationProperty configurationProperty,
      final String actionSuggested,
      final Object configuredValue,
      final Object currentValue) {
    this.description = description;

    this.configurationProperty = configurationProperty;
    this.actionSuggested = actionSuggested;
    this.configuredValue = configuredValue;
    this.currentValue = currentValue;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Tip tip = (Tip) o;
    return Objects.equals(description, tip.description)
        && configurationProperty == tip.configurationProperty
        && actionSuggested == tip.actionSuggested
        && Objects.equals(configuredValue, tip.configuredValue)
        && Objects.equals(currentValue, tip.currentValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        description, configurationProperty, actionSuggested, configuredValue, currentValue);
  }

  @Override
  public String toString() {
    return "Tip{"
        + "description=["
        + description
        + "]"
        + "; configurationProperty=["
        + configurationProperty
        + "]"
        + "; actionSuggested=["
        + actionSuggested
        + "]"
        + "; configuredValue=["
        + configuredValue
        + "]"
        + "; currentValue=["
        + currentValue
        + "]"
        + '}';
  }



  public enum ConfigurationProperty {
    ActivityStartToClose
  }
}
