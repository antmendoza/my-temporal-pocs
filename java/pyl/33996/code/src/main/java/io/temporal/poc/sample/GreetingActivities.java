package io.temporal.poc.sample;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface GreetingActivities {

    String composeGreeting(String name);
}
