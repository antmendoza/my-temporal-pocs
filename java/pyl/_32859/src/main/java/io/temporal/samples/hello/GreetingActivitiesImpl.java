package io.temporal.samples.hello;

public class GreetingActivitiesImpl implements GreetingActivities {
    @Override
    public String composeGreeting(String name) {


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Hello " + name + "!";
    }
}
