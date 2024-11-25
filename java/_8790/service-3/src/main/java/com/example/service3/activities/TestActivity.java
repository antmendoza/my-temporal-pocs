package com.example.service3.activities;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface TestActivity {
    String printHello();
}
