package io.antmendoza.samples._20231205;

import java.util.Objects;

public class UIData {
    private String value;
    public UIData() {

    }

    public UIData(String value) {
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UIData uiData = (UIData) o;
        return Objects.equals(value, uiData.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
