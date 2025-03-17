package com.temporal.workflow;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface MyActivity {


    String doSomething(String name);

}
