package io.temporal.samples.hello.new_;

import io.temporal.common.converter.EncodedValues;

public class MyDynamicActivityComplexType implements HelloDynamicActivityTest.MyStaticActivity<ComplexType> {

    private ComplexType name = new ComplexType("name", 1);

    public MyDynamicActivityComplexType(ComplexType name) {
        this.name = name;
    }

    public MyDynamicActivityComplexType() {
    }

    @Override
    public ComplexType execute(final EncodedValues args) {
        return name;
    }
}