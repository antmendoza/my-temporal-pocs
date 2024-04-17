package io.antmendoza.samples._5611;

public class Activity_5611Impl implements Activity_5611 {
    @Override
    public String doSomething() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Running activity >>>>> ");
        return "something";
    }
}