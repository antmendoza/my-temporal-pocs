package com.temporal.demos.temporalspringbootdemo.workflows.activity;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = "HelloSampleTaskQueue")
public class MyActivityImpl implements MyActivity {
    @Override
    public String doSomething() {
        return this.getClass().getPackageName();

    }
}
