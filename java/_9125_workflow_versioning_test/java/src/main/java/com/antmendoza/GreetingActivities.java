package com.antmendoza;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface GreetingActivities {

  // Define your activity method which can be called during workflow execution
  @ActivityMethod(name = "greet")
  String composeGreeting(String greeting, String name);
}
