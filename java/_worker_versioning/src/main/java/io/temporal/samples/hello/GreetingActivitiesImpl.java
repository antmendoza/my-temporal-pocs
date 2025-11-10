package io.temporal.samples.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreetingActivitiesImpl implements GreetingActivities {
    private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

    @Override
    public String sleepSeconds(int seconds) {
        //log.info("Composing greeting...");

        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return seconds + " seconds sleeping..!";
    }


}
