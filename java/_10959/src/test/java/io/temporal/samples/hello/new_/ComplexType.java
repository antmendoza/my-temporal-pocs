package io.temporal.samples.hello.new_;

import java.util.Objects;

public class ComplexType {
    private String name;
    private int age;

    public ComplexType() {
    }

    public ComplexType(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }


    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final ComplexType that = (ComplexType) o;
        return age == that.age && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
