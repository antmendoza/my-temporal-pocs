package com.example.servicespringboot.activities;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = {"TASK_QUEUE"})
public class TestActivityImpl implements TestActivity {
    @Override
    public String printHello() {
        return MDC.get("traceId");
    }
}
