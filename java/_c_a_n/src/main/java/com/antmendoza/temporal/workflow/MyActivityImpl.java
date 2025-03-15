package com.antmendoza.temporal.workflow;

public class MyActivityImpl implements MyActivity {
    @Override
    public String doSomething(final String name) {

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "";
    }
}
