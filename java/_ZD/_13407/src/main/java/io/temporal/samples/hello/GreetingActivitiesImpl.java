package io.temporal.samples.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreetingActivitiesImpl implements GreetingActivities {
    private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

    @Override
    public String doSomething(String greeting) {
        log.info("Composing greeting...");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return greeting + "!";
    }
}
