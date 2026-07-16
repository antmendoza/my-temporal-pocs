package io.temporal.poc.sample;

public class GreetingActivitiesImpl implements GreetingActivities {

    @Override
    public String composeGreeting(String name) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Hello, " + name;
    }
}