package com.example.servicespringboot.activities;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface TestActivity {
    String printHello();
}
