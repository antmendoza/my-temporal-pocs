package com.antmendoza.temporal;

import io.temporal.activity.Activity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreetingActivitiesImpl implements GreetingActivities {

  private static int num_attempts = 0;

  private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);


  @Override
  public MyActivityResult activity_1(MyActivityInput input) {
    return getResult();
  }

  @Override
  public MyActivityResult activity_2(MyActivityInput input) {
    return getResult();
  }

  @Override
  public MyActivityResult activity_3(MyActivityInput input) {
    return getResult();
  }

  private static @NonNull MyActivityResult getResult() {
    String activityType = Activity.getExecutionContext().getInfo().getActivityType();
    log.info("Activity {} invoked ", activityType);

    return new MyActivityResult(activityType + " success");
  }

  @Override
  public void auditLogging() {
    num_attempts ++;
    // simulate some random exception if attempt is less than 3
    if(num_attempts < 10) {
      throw new RuntimeException("some random exception pushing logs");
    }
  }
}
