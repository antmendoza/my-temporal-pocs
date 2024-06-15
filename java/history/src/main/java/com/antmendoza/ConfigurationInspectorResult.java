package com.antmendoza;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationInspectorResult {


    private final List<Tip> tips = new ArrayList<>();

    public List<Tip> getTips() {
        return tips;

    }

    public void addTip(final List<Tip> tips) {
        this.tips.addAll(tips);
    }


    @Override
    public String toString() {

        String tPrety = "";

        for (Tip tip : tips) {
            tPrety += " " + tip + System.lineSeparator();
        }

        return "ConfigurationInspectorResult{" +
                "tips=" + System.lineSeparator()
                + tPrety +
                '}';
    }
}
