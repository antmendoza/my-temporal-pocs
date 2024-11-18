package com.example.service2.activities;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface TestActivity {
    String printHello();
}
