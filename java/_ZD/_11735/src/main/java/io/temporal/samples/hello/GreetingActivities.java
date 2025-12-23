package io.temporal.samples.hello;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface GreetingActivities {

    // Define your activity method which can be called during workflow execution
    @ActivityMethod(name = "greet")
    String sleep5Seconds(String greeting, String name);
}
