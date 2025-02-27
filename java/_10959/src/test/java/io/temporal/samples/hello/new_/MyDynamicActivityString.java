package io.temporal.samples.hello.new_;


import io.temporal.common.converter.EncodedValues;

public class MyDynamicActivityString implements HelloDynamicActivityTest.MyStaticActivity<String> {


    @Override
    public String execute(final EncodedValues args) {
        return "done";
    }
}
