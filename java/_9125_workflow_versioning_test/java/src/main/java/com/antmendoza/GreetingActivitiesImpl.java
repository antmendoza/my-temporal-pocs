package com.antmendoza;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Simple activity implementation, that concatenates two strings. */
public class GreetingActivitiesImpl implements GreetingActivities {
  private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

  @Override
  public String composeGreeting(String greeting, String name) {
    log.info("Composing greeting...");
    return greeting + " " + name + "!";
  }
}
