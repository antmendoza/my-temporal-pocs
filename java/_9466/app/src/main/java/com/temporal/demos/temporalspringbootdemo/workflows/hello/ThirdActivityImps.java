package com.temporal.demos.temporalspringbootdemo.workflows.hello;

import com.temporal.demos.temporalspringbootdemo.workflows.hello.model.Person;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = {"otherTaskqueue"})
public class ThirdActivityImps implements ThirdActivity {

  @Override
  public String thirdActivity(final Person person) {
    return "";
  }
}
