package com.temporal.demos.temporalspringbootdemo.workflows.activity_v2;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;

@Component("MyActivity_v2")
@ActivityImpl(taskQueues = "HelloSampleTaskQueue")
public class MyActivityImpl implements MyActivity {
    @Override
    public String doSomething() {
        return this.getClass().getPackageName();
    }
}
