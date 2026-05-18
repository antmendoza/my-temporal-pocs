package com.antmendoza.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface GreetingActivities {

  @ActivityMethod
  MyActivityResult activity_1(MyActivityInput input);



  @ActivityMethod
  MyActivityResult activity_2(MyActivityInput input);



  @ActivityMethod
  MyActivityResult activity_3(MyActivityInput input);


  @ActivityMethod
  void auditLogging();

}
