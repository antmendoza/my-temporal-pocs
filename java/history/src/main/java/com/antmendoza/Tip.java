package com.antmendoza;

import java.util.Objects;

public class Tip {
    private final ConfigurationProperty configurationProperty;
    private final ActionSuggested actionSuggested;
    private final Object configuredValue;
    private final Object currentValue;

    public Tip(final ConfigurationProperty configurationProperty,
               final ActionSuggested actionSuggested,
               final Object configuredValue,
               final Object currentValue) {

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
        return configurationProperty == tip.configurationProperty && actionSuggested == tip.actionSuggested && Objects.equals(configuredValue, tip.configuredValue) && Objects.equals(currentValue, tip.currentValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configurationProperty, actionSuggested, configuredValue, currentValue);
    }


    @Override
    public String toString() {
        return "Tip{" +
                "configurationProperty=" + configurationProperty +
                ", actionSuggested=" + actionSuggested +
                ", configuredValue=" + configuredValue +
                ", currentValue=" + currentValue +
                '}';
    }
}
