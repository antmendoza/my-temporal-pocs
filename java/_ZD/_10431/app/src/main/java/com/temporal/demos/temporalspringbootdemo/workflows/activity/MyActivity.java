package com.temporal.demos.temporalspringbootdemo.workflows.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MyActivity {
    @ActivityMethod
    String doSomething();

}
