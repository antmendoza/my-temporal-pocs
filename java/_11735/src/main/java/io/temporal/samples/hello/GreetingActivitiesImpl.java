package io.temporal.samples.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreetingActivitiesImpl implements GreetingActivities {
    private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

    @Override
    public String composeGreeting(String greeting, String name) {
        log.info("Composing greeting...");
        return greeting + " " + name + "!";
    }
}
